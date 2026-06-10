package com.pajamas.stock.service.impl;

import cn.hutool.json.JSONArray;
import com.pajamas.api.dto.stock.StockItemDTO;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.exceptions.DbException;
import com.pajamas.common.utils.JsonUtils;
import com.pajamas.stock.domain.po.Stock;
import com.pajamas.stock.domain.po.StockDeductList;
import com.pajamas.stock.enums.TCCStatus;
import com.pajamas.stock.mapper.StockDeductListMapper;
import com.pajamas.stock.service.IStockDeductListService;
import com.pajamas.stock.service.IStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * TCC事务记录List 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductListServiceImpl extends ServiceImpl<StockDeductListMapper, StockDeductList> implements IStockDeductListService {

    private final IStockService stockService;

    @Override
    @Transactional
    public boolean batchTry(BusinessActionContext context, List<StockItemDTO> items, String orderNo, LocalDateTime expireTime) {
        //1.获取全局与分支事务id
        String txId = RootContext.getXID();
        Long branchId = context.getBranchId();
        //2.尝试提前插入，幂等唯一索引防重复
        StockDeductList record = new StockDeductList()
                .setTxId(txId)
                .setBranchId(branchId)
                .setOrderNo(orderNo)
                .setItemsJson(JsonUtils.toJsonStr(items))
                .setExpireTime(expireTime)
                .setStatus(TCCStatus.TRIED);
        try {
            save(record);
        } catch (DuplicateKeyException e) {
            //3.冲突：说明记录已存在，查状态
            StockDeductList existing = getBaseMapper().selectStatus(txId, branchId);
            //3.1防悬挂：Try 执行时查事务表，发现已有 Cancel 记录，直接拒绝
            //Cancel 比 Try 先到，不拦住 Try 资金永久冻结
            if (existing.getStatus() == TCCStatus.CANCELLED) {
                log.warn("防悬挂：Try 在 Cancel 之后执行, txId={}", txId);
                return false;
            }
            //3.2幂等：已存在
            log.info("Try 幂等：流水已存在, txId={}", txId);
            return true;
        }
        //4.插入成功，逐商品冻结库存
        for (StockItemDTO item : items) {
            Stock stock = stockService.lambdaQuery()
                    .select(Stock::getSkuId)
                    .eq(Stock::getSkuId, item.getSkuId())
                    .one();
            if (stock == null) {
                throw new BadRequestException("商品不存在: skuId=" + item.getSkuId());
            }
            //5.冻结库存 乐观锁
            //UPDATE stock SET frozen = frozen + #{qty}
            //WHERE sku_id = #{skuId} AND quantity - sold - frozen >= #{qty}
            boolean updated = stockService.lambdaUpdate()
                    .eq(Stock::getSkuId, item.getSkuId())
                    .apply("quantity - sold - frozen >= {0}", item.getQuantity())
                    .setSql("frozen = frozen + " + item.getQuantity())
                    .update();
            if (!updated) {
                throw new DbException("Try 扣库存失败，可能库存不足: skuId=" + item.getSkuId());
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean batchConfirm(BusinessActionContext context) {
        //1.获取全局与分支事务id
        String txId = context.getXid();
        Long branchId = context.getBranchId();
        //2.尝试更新状态 乐观锁
        boolean update = lambdaUpdate()
                .set(StockDeductList::getStatus, TCCStatus.CONFIRMED)
                .eq(StockDeductList::getTxId, txId)
                .eq(StockDeductList::getBranchId, branchId)
                .eq(StockDeductList::getStatus, TCCStatus.TRIED)
                .update();
        if (!update) {
            //3.更新失败，说明状态已修改，查状态
            StockDeductList existing = getBaseMapper().selectStatus(txId, branchId);
            //3.1幂等：状态机 或通过 全局事务 id 做唯一索引校验（额外维护confirm表）
            if (existing.getStatus() == TCCStatus.CONFIRMED) {
                log.info("Confirm 幂等：流水已修改, txId={}", txId);
                return true;
            }
            if (existing.getStatus() == TCCStatus.CANCELLED) {
                throw new BadRequestException("事务记录状态未异常");
            }
        }
        //4.更新成功，查询记录
        StockDeductList existing = lambdaQuery()
                .select(StockDeductList::getItemsJson)
                .eq(StockDeductList::getTxId, txId)
                .eq(StockDeductList::getBranchId, branchId)
                .one();
        List<StockItemDTO> items = parseItems(existing.getItemsJson());
        //5.逐商品确认扣减
        for (StockItemDTO item : items) {
            boolean updated = stockService.lambdaUpdate()
                    .eq(Stock::getSkuId, item.getSkuId())
                    .apply("frozen >= {0}", item.getQuantity())
                    .setSql("frozen = frozen - " + item.getQuantity())
                    .setSql("sold = sold + " + item.getQuantity())
                    .update();
            if (!updated) {
                throw new DbException("Confirm 更新库存失败，可能 frozen 不足");
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean batchCancel(BusinessActionContext context) {
        //1.获取全局与分支事务id
        String txId = context.getXid();
        Long branchId = context.getBranchId();
        //2.尝试更新状态 乐观锁
        boolean update = lambdaUpdate()
                .set(StockDeductList::getStatus, TCCStatus.CANCELLED)
                .eq(StockDeductList::getTxId, txId)
                .eq(StockDeductList::getBranchId, branchId)
                .eq(StockDeductList::getStatus, TCCStatus.TRIED)
                .update();
        if (!update) {
            //3.更新失败，说明状态已修改或不存在，查状态
            StockDeductList existing = getBaseMapper().selectStatus(txId, branchId);
            //3.1不存在
            if (existing == null) {
                //3.2防悬挂：插入cancel记录
                //网络拥堵，Cancel 比 Try 先到，不拦住 Try 资金永久冻结
                save(new StockDeductList().setTxId(txId)
                        .setBranchId(branchId)
                        .setStatus(TCCStatus.CANCELLED)
                        .setItemsJson("[]")
                        .setExpireTime(LocalDateTime.now()));
                //3.3空回滚：请求丢包,Try 没执行，TC 发起了Cancel
                //事务控制表，Cancel 执行前查不到 Try 记录直接返回成功
                log.info("Cancel 空回滚：Try 未执行, txId={}", txId);
                return true;
            }
            //3.4已修改 幂等：状态机 或通过 全局事务 id 做唯一索引校验（额外维护confirm表）
            if (existing.getStatus() == TCCStatus.CANCELLED) {
                log.info("Cancel 幂等：流水已修改, txId={}", txId);
                return true;
            }
            if (existing.getStatus() == TCCStatus.CONFIRMED) {
                throw new BadRequestException("事务记录状态未异常");
            }
        }
        //4.更新成功，查询记录
        StockDeductList existing = lambdaQuery()
                .select(StockDeductList::getItemsJson)
                .eq(StockDeductList::getTxId, txId)
                .eq(StockDeductList::getBranchId, branchId)
                .one();
        List<StockItemDTO> items = parseItems(existing.getItemsJson());
        //5.逐商品恢复库存
        for (StockItemDTO item : items) {
            boolean updated = stockService.lambdaUpdate()
                    .eq(Stock::getSkuId, item.getSkuId())
                    .apply("frozen >= {0}", item.getQuantity())
                    .setSql("frozen = frozen - " + item.getQuantity())
                    .update();
            if (!updated) {
                throw new DbException("Cancel 释放冻结失败，可能 frozen 不足");
            }
        }
        return true;
    }

    private List<StockItemDTO> parseItems(String itemsJson) {
        JSONArray arr = JsonUtils.parseArray(itemsJson);
        return arr.toList(StockItemDTO.class);
    }
}
