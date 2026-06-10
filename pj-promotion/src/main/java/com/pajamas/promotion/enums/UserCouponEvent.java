package com.pajamas.promotion.enums;

public enum UserCouponEvent {
    RECEIVE,        // 领取优惠券
    LOCK,           // 下单时锁定优惠券
    UNLOCK,         // 取消订单/超时未支付，释放锁定
    USE,            // 支付成功，核销优惠券
    REFUND,         // 退款，退回优惠券（可能恢复可用）
    EXPIRE          // 过期（定时任务触发）
}