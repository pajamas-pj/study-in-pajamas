package com.pajamas.stock.service;

import com.pajamas.api.dto.stock.StockTryDeductDTO;
import com.pajamas.stock.domain.po.StockDeduct;
import com.baomidou.mybatisplus.extension.service.IService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * TCC事务记录 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-03
 */
@LocalTCC
public interface IStockDeductService extends IService<StockDeduct> {
    @TwoPhaseBusinessAction(name = "stockTCC", commitMethod = "confirm", rollbackMethod = "cancel")
    boolean tryDeduct(BusinessActionContext context,
                      Long skuId,
                      Integer quantity,
                      String orderNo,
                      LocalDateTime expireTime);
    boolean confirm(BusinessActionContext context);
    boolean cancel(BusinessActionContext context);

}
