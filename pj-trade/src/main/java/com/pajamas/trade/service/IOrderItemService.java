package com.pajamas.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.trade.domain.po.Order;
import com.pajamas.trade.domain.po.OrderItem;
import com.pajamas.trade.domain.po.RefundApply;
import com.pajamas.trade.domain.vo.OrderProgressNodeVO;
import com.pajamas.trade.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单明细 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
public interface IOrderItemService extends IService<OrderItem> {

    List<OrderItem> queryByOrderId(Long id);

    List<OrderProgressNodeVO> packageProgressNodes(Order order, RefundApply refundApply);

    void updateStatusByOrderId(Long orderId, OrderStatus orderStatus);

    void markDetailSuccessByOrderId(Long id, String payChannel, LocalDateTime successTime);

    List<Long> queryCourseIdsByOrderId(Long id);
}
