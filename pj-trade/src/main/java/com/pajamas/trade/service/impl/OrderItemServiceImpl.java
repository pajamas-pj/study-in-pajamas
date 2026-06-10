package com.pajamas.trade.service.impl;

import com.pajamas.trade.domain.po.OrderItem;
import com.pajamas.trade.mapper.OrderItemMapper;
import com.pajamas.trade.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

}
