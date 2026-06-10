package com.pajamas.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.trade.domain.dto.OrderCreateDTO;
import com.pajamas.trade.domain.po.Order;
import com.pajamas.trade.domain.vo.OrderCreateVO;

public interface IOrderService extends IService<Order> {

    String getIdempotentToken();

    OrderCreateVO createOrder(OrderCreateDTO dto);
}
