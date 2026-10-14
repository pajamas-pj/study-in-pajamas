package com.pajamas.trade.service.impl;

import cn.hutool.db.DbRuntimeException;
import com.pajamas.common.constants.ErrorInfo;
import com.pajamas.trade.domain.po.Order;
import com.pajamas.trade.domain.po.OrderItem;
import com.pajamas.trade.domain.po.RefundApply;
import com.pajamas.trade.domain.vo.OrderProgressNodeVO;
import com.pajamas.trade.enums.OrderStatus;
import com.pajamas.trade.enums.RefundStatus;
import com.pajamas.trade.mapper.OrderItemMapper;
import com.pajamas.trade.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.pajamas.trade.enums.OrderStatus.FINISHED;
import static com.pajamas.trade.enums.OrderStatus.PAYED;

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

    @Override
    public List<OrderItem> queryByOrderId(Long id) {
        return lambdaQuery().eq(OrderItem::getOrderId,id).list();
    }

    @Override
    public List<OrderProgressNodeVO> packageProgressNodes(Order order, RefundApply refundApply) {
        // 1.先填充订单交易的每个节点的时间值
        List<OrderProgressNodeVO> list = new ArrayList<>();
        // 1.1.下单时间
        list.add(new OrderProgressNodeVO(OrderStatus.NO_PAY.getProgressName(), order.getCreateTime()));
        // 1.2.支付成功时间
        list.add(new OrderProgressNodeVO(PAYED.getProgressName(), order.getPayTime()));
        // 1.3.交易关闭时间
        list.add(new OrderProgressNodeVO(OrderStatus.CLOSED.getProgressName(), order.getCloseTime()));
        // 1.4.交易完成时间
        list.add(new OrderProgressNodeVO(FINISHED.getProgressName(), order.getFinishTime()));
        if (refundApply != null) {
            // 1.5.没有退款参数情况下，默认是用户端查询，添加退款成功时间字段
            list.add(new OrderProgressNodeVO(OrderStatus.REFUNDED.getProgressName(), refundApply.getCreateTime()));
            // 2.再填充订单退款的每个节点的时间值
            // 2.1.订单申请退款
            list.add(new OrderProgressNodeVO(RefundStatus.UN_APPROVE.getProgressName(), refundApply.getCreateTime()));
            // 2.2.订单审批时间（审批成功或失败）
            list.add(new OrderProgressNodeVO(RefundStatus.AGREE.getProgressName(), refundApply.getApproveTime()));
            // 2.3.退款结束时间（学员取消、退款成功、退款关闭）
            RefundStatus status = RefundStatus.of(refundApply.getStatus().getValue());
            list.add(new OrderProgressNodeVO(status.getProgressName(), refundApply.getFinishTime()));
        }
        // 3.然后过滤掉没有时间的节点，再按照时间进行排序，升序
        return list.stream()
                .filter(nodeVO -> nodeVO.getTime() != null)
                .sorted(Comparator.comparing(OrderProgressNodeVO::getTime))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatusByOrderId(Long orderId, OrderStatus status) {
        boolean success = lambdaUpdate()
                .set(OrderItem::getItemStatus, status)
                .eq(OrderItem::getOrderId, orderId)
                .update();
        if (!success) {
            throw new DbRuntimeException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public void markDetailSuccessByOrderId(Long id, String payChannel, LocalDateTime successTime) {
        List<OrderItem> details = queryByOrderId(id);
        for (OrderItem detail : details) {
            detail.setItemStatus(PAYED);
            detail.setPayChannel(payChannel);
            detail.setCourseExpireTime(successTime.plusMinutes(detail.getValidDuration()));
        }
        updateBatchById(details);
    }

    public List<Long> queryCourseIdsByOrderId(Long orderId) {
        return baseMapper.queryCourseIdsByOrderId(orderId);
    }
}
