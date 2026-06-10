package com.pajamas.promotion.service;

import com.pajamas.promotion.domain.po.CouponTccLog;
import com.baomidou.mybatisplus.extension.service.IService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.util.List;

/**
 * <p>
 * 优惠券TCC事务记录 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-08
 */
@LocalTCC
public interface ICouponTccLogService extends IService<CouponTccLog> {

    /**
     * Try — 批量锁定优惠券
     *
     * @param context       Seata 事务上下文
     * @param userCouponIds 用户优惠券id列表
     * @param orderNo       订单号
     */
    @TwoPhaseBusinessAction(name = "couponTcc", commitMethod = "batchConfirm", rollbackMethod = "batchCancel")
    boolean batchTry(BusinessActionContext context,
                     List<Long> userCouponIds,
                     String orderNo);

    /**
     * Confirm — 批量确认核销
     */
    boolean batchConfirm(BusinessActionContext context);

    /**
     * Cancel — 批量回滚解锁
     */
    boolean batchCancel(BusinessActionContext context);
}
