package com.pajamas.stock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.stock.domain.po.Stock;
import com.pajamas.stock.domain.po.StockDeduct;
import com.pajamas.stock.enums.TCCStatus;
import com.pajamas.stock.mapper.StockDeductMapper;
import com.pajamas.stock.service.IStockDeductService;
import com.pajamas.stock.service.IStockService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.metadata.DatabaseException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * TCC事务记录 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductServiceImpl extends ServiceImpl<StockDeductMapper, StockDeduct> implements IStockDeductService {

    private final IStockService stockService;
    @Override
    @Transactional
    public boolean tryDeduct(BusinessActionContext context, Long skuId, Integer quantity, String orderNo, LocalDateTime expireTime) {

        //1获取全局与分支事务id
        String txId = RootContext.getXID();
        Long branchId = context.getBranchId();
        //2.尝试提前插入，幂等唯一索引防重复
        StockDeduct stockDeduct = new StockDeduct()
                .setTxId(txId)
                .setBranchId(branchId)
                .setSkuId(skuId)
                .setQuantity(quantity)
                .setOrderNo(orderNo)
                .setStatus(TCCStatus.TRIED);
        try {
            save(stockDeduct);
        } catch (DuplicateKeyException e) {
            //3.冲突：说明记录已存在，查状态
            StockDeduct existing = getBaseMapper().selectStatus(txId, branchId);
            //3.1防悬挂：Try 执行时查事务表，发现已有 Cancel 记录，直接拒绝
            //Cancel 比 Try 先到，不拦住 Try 资金永久冻结
            if (existing.getStatus() == TCCStatus.CANCELLED) {
                log.warn("防悬挂：Try 在 Cancel 之后执行, txId={}", txId);
                return false;
            }
            //3.2幂等：已存在
            if (existing.getStatus() == TCCStatus.TRIED || existing.getStatus() == TCCStatus.CONFIRMED) {
                log.info("Try 幂等：流水已存在, txId={}", txId);
                return true;
            }
        }
        //4.插入成功，查询库存
        Stock stock = stockService.lambdaQuery()
                .select(Stock::getSkuId)
                .eq(Stock::getSkuId, skuId).one();
        if (stock == null){
            throw new BadRequestException("商品不存在");
        }
        //5.冻结库存 乐观锁
        //UPDATE stock SET frozen = frozen + #{quantity}b
        //WHERE sku_id = #{skuId} AND quantity - sold - frozen >= #{quantity}
        boolean updated = stockService.lambdaUpdate()
                .eq(Stock::getSkuId, skuId)
                .apply("quantity - sold - frozen >= {0}", quantity)
                .setSql("frozen = frozen + " + quantity)
                .update();
        if (!updated){
            throw new BadRequestException("Try 扣库存失败，可能库存不足: skuId=" + skuId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean confirm(BusinessActionContext context) {

        //1获取全局与分支事务id
        String txId = context.getXid();
        Long branchId = context.getBranchId();
        //2.尝试更新状态 乐观锁
        boolean update = lambdaUpdate()
                .set(StockDeduct::getStatus, TCCStatus.CONFIRMED)
                .eq(StockDeduct::getTxId, txId)
                .eq(StockDeduct::getBranchId, branchId)
                .eq(StockDeduct::getStatus, TCCStatus.TRIED)
                .update();
        if(!update){
            // 3.更新失败，说明状态已修改，查状态
            StockDeduct existing = getBaseMapper().selectStatus(txId, branchId);
            //3.1幂等：状态机 或通过 全局事务 id 做唯一索引校验（额外维护confirm表）
            if (existing.getStatus() == TCCStatus.CONFIRMED) {
                log.info("Confirm 幂等：流水已修改, txId={}", txId);
                return true;
            }
            if(existing.getStatus() == TCCStatus.CANCELLED){
                throw new BadRequestException("事务记录状态未异常");
            }
        }
        //4.插入成功，查询记录
        StockDeduct existing = getBaseMapper().selectSkuIdAndQuantity(txId, branchId);
        boolean updated = stockService.lambdaUpdate()
                .eq(Stock::getSkuId, existing.getSkuId())
                .apply("frozen >= {0}", existing.getQuantity())
                .setSql("frozen = frozen - " + existing.getQuantity())
                .setSql("sold = sold + " + existing.getQuantity())
                .update();
        if (!updated) {
            throw new DatabaseException("Confirm 更新库存失败，可能 frozen 不足");
        }
        return true;
    }

    @Override
    @Transactional
    public boolean cancel(BusinessActionContext context) {
        //1获取全局与分支事务id
        String txId = context.getXid();
        Long branchId = context.getBranchId();
        //2.尝试更新状态 乐观锁
        boolean update = lambdaUpdate().set(StockDeduct::getStatus, TCCStatus.CANCELLED)
                .eq(StockDeduct::getTxId, txId)
                .eq(StockDeduct::getBranchId, branchId)
                .eq(StockDeduct::getStatus, TCCStatus.TRIED)
                .update();
        if(!update){
            // 3.更新失败，说明状态已修改或不存在，查状态
            StockDeduct existing = getBaseMapper().selectStatus(txId, branchId);
            //3.1不存在
            if(existing == null){
                //3.2防悬挂：插入cancel记录
                //网络拥堵，Cancel 比 Try 先到，不拦住 Try 资金永久冻结
                save(new StockDeduct().setTxId(txId)
                        .setBranchId(branchId)
                        .setStatus(TCCStatus.CANCELLED)
                        .setSkuId(0L)
                        .setQuantity(0)
                        .setExpireTime(LocalDateTime.now()));
                //3.3.空回滚：请求丢包,Try 没执行，TC 发起了Cancel
                //事务控制表，Cancel 执行前查不到 Try 记录直接返回成功
                log.info("Cancel 空回滚：Try 未执行, txId={}", txId);
                return true;
            }
            //3.4已修改 幂等：状态机 或通过 全局事务 id 做唯一索引校验（额外维护confirm表）
            if (existing.getStatus() == TCCStatus.CANCELLED) {
                log.info("Cancel 幂等：流水已修改, txId={}", txId);
                return true;
            }
            if(existing.getStatus() == TCCStatus.CONFIRMED){
                throw new BadRequestException("事务记录状态未异常");
            }
        }
        //6.更新成功，恢复库存
        StockDeduct existing = getBaseMapper().selectSkuIdAndQuantity(txId, branchId);
        boolean updated = stockService.lambdaUpdate()
                .eq(Stock::getSkuId, existing.getSkuId())
                .apply("frozen >= {0}", existing.getQuantity())
                .setSql("frozen = frozen - " + existing.getQuantity())
                .update();
        if (!updated) {
            throw new DatabaseException("Cancel 释放冻结失败，可能 frozen 不足");
        }
        return true;
    }
}
