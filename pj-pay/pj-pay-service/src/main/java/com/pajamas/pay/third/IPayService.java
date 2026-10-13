package com.pajamas.pay.third;


import com.pajamas.pay.third.model.*;

/**
 * 统一支付服务接口
 */
public interface IPayService {

    PrepayResponse createPrepayOrder(String title, String orderNo, Integer amount);

    PayLoadResponse code2Session(String code);

    PayStatusResponse queryPayOrderStatus(String payOrderNo);

    RefundResponse refundOrder(String payOrderNo, String refundOrderNo, Integer refundAmount, Integer totalAmount);

    RefundResponse queryRefundStatus(String orderNo, String refundOrderNo);

    PrepayResponse createWxMiniPrepayOrder(String title, String orderNo, Integer amount, String openid);

    PayCloseResponse closeWxOrder(String payOrderNo);

}
