package com.pajamas.pay.domain.enums;

public enum PayOrderEvent {
    CREATE_ORDER,   // 创建订单
    PAY_SUCCESS,    // 支付成功
    PAY_TIMEOUT,    // 支付超时
    CLOSE_ORDER,    // 关闭订单
    CANCEL_ORDER    // 取消订单
}
