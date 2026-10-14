package com.pajamas.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.pay.sdk.dto.PayResultDTO;
import com.pajamas.trade.constants.OrderCancelReason;
import com.pajamas.trade.domain.dto.OrderCreateDTO;
import com.pajamas.trade.domain.po.Order;
import com.pajamas.trade.domain.vo.OrderConfirmVO;
import com.pajamas.trade.domain.vo.OrderCreateVO;
import com.pajamas.trade.domain.vo.OrderVO;

import java.util.List;

public interface IOrderService extends IService<Order> {

    String getIdempotentToken();

    OrderCreateVO createOrder(OrderCreateDTO dto);

    OrderConfirmVO prePlaceOrder(List<Long> courseIds);

    OrderVO queryOrderById(Long id);

    void cancelOrder(Long orderId, OrderCancelReason orderCancelReason);

    void handlePaySuccess(PayResultDTO payResult);
}
