package com.pajamas.stock.service;

import com.pajamas.api.dto.stock.StockItemDTO;
import com.pajamas.stock.domain.po.StockDeductList;
import com.baomidou.mybatisplus.extension.service.IService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * TCC事务记录List 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-07
 */
@LocalTCC
public interface IStockDeductListService extends IService<StockDeductList> {

    /**
     * Try — 批量预扣库存
     *
     * @param context    Seata 事务上下文
     * @param items      商品列表
     * @param orderNo    订单号
     * @param expireTime 过期时间
     */
    @TwoPhaseBusinessAction(name = "stockListTCC", commitMethod = "batchConfirm", rollbackMethod = "batchCancel")
    boolean batchTry(BusinessActionContext context,
                     List<StockItemDTO> items,
                     String orderNo,
                     LocalDateTime expireTime);

    /**
     * Confirm — 批量确认扣减
     */
    boolean batchConfirm(BusinessActionContext context);

    /**
     * Cancel — 批量回滚预扣
     */
    boolean batchCancel(BusinessActionContext context);
}
