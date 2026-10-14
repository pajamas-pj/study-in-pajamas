package com.pajamas.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.api.client.course.CourseClient;
import com.pajamas.api.client.promotion.CouponTccClient;
import com.pajamas.api.client.promotion.PromotionClient;
import com.pajamas.api.client.stock.StockDeductListClient;
import com.pajamas.api.constants.CourseStatus;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.api.dto.promotion.CouponDiscountDTO;
import com.pajamas.api.dto.promotion.CouponTccFormDTO;
import com.pajamas.api.dto.promotion.OrderCouponDTO;
import com.pajamas.api.dto.promotion.OrderCourseDTO;
import com.pajamas.api.dto.stock.StockDeductListFormDTO;
import com.pajamas.api.dto.stock.StockItemDTO;
import com.pajamas.api.dto.trade.OrderBasicDTO;
import com.pajamas.common.config.mq.RabbitMqHelper;
import com.pajamas.common.constants.MqConstants;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.exceptions.BizIllegalException;
import com.pajamas.common.exceptions.DbException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.pay.sdk.dto.PayResultDTO;
import com.pajamas.trade.config.TradeProperties;
import com.pajamas.trade.constants.OrderCancelReason;
import com.pajamas.trade.constants.TradeErrorInfo;
import com.pajamas.trade.domain.dto.CartUserDTO;
import com.pajamas.trade.domain.dto.OrderCreateDTO;
import com.pajamas.trade.domain.dto.OrderItemDTO;
import com.pajamas.trade.domain.po.Order;
import com.pajamas.trade.domain.po.OrderItem;
import com.pajamas.trade.domain.po.OrderLog;
import com.pajamas.trade.domain.po.RefundApply;
import com.pajamas.trade.domain.vo.*;
import com.pajamas.trade.enums.OrderStatus;
import com.pajamas.trade.enums.RefundStatus;
import com.pajamas.trade.mapper.OrderMapper;
import com.pajamas.trade.service.IOrderItemService;
import com.pajamas.trade.service.IOrderService;
import com.pajamas.trade.service.IRefundApplyService;
import com.pajamas.trade.utils.IdempotentHelper;
import com.pajamas.trade.utils.RedisIdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.pajamas.trade.constants.TradeErrorInfo.ORDER_ALREADY_FINISH;
import static com.pajamas.trade.constants.TradeErrorInfo.ORDER_NOT_EXISTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService{

    private final PromotionClient promotionClient;
    private final CourseClient courseClient;
    private final CouponTccClient couponTccClient;
    private final StockDeductListClient stockDeductListClient;

    private final IOrderItemService orderItemService;
    private final IRefundApplyService refundApplyService;
    private final TradeProperties tradeProperties;
    private final RedisIdWorker redisIdWorker;
    private final IdempotentHelper idempotentHelper;
    private final RabbitMqHelper mqHelper;
    @Override
    @GlobalTransactional
    public OrderCreateVO createOrder(OrderCreateDTO orderCreateDTO) {
        //1.幂等校验 redisToken先斩后奏
        Long userId = UserContext.getUser();
        Boolean result = idempotentHelper.checkIdempotentToken(userId, orderCreateDTO.getIdempotentToken(), "order:");
        if (!result) {
            throw new BizIllegalException("订单已提交，请勿重复操作");
        }
        //1.1生成业务订单号
        Long orderId = redisIdWorker.nextId("order");
        //1.2订单关闭时间
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(tradeProperties.getPayOrderTTLMinutes());
        //2.查询商品费用信息
        //2.1收集商品id
        List<OrderItemDTO> items = orderCreateDTO.getItems();
        if(CollUtils.isEmpty(items)){
            throw new BizIllegalException("订单商品不能为空");
        }
        List<Long> courseIds = items.stream().map(OrderItemDTO::getSkuId).collect(Collectors.toList());
        //2.2查询课程费用信息，如果不可购买，这里直接报错
        List<CourseSimpleInfoDTO> courseInfos = getOnShelfCourse(courseIds);
        //3.TCC库存冻结
        List<StockItemDTO> stockItems = BeanUtils.copyList(items, StockItemDTO.class);
        StockDeductListFormDTO formDTO = new StockDeductListFormDTO();
        formDTO.setItems(stockItems);
        formDTO.setOrderNo(orderId.toString());
        formDTO.setExpireTime(expireTime);
        boolean isTCC = stockDeductListClient.batchTry(formDTO);
        if (!isTCC) {
            throw new BizIllegalException("库存不足");
        }
        //4.TCC用户券冻结
        CouponTccFormDTO couponTccFormDTO = new CouponTccFormDTO();
        couponTccFormDTO.setUserCouponIds(orderCreateDTO.getCouponIds());
        couponTccFormDTO.setOrderNo(orderId.toString());
        boolean isCoupon = couponTccClient.batchTry(couponTccFormDTO);
        if (!isCoupon) {
            throw new BizIllegalException("优惠券无效");
        }
        //5.封装订单信息
        Order order = new Order();
        // 5.1.计算订单金额
        Integer totalAmount = courseInfos.stream()
                .map(CourseSimpleInfoDTO::getPrice).reduce(Integer::sum).orElse(0);
        // 5.2.计算优惠金额
        order.setDiscountAmount(0);
        List<Long> couponIds = orderCreateDTO.getCouponIds();
        CouponDiscountDTO discount = null;
        if (CollUtils.isNotEmpty(couponIds)) {
            List<OrderCourseDTO> orderCourses = courseInfos.stream()
                    .map(c -> new OrderCourseDTO().setId(c.getId()).setCateId(c.getThirdCateId()).setPrice(c.getPrice()))
                    .collect(Collectors.toList());
            discount = promotionClient.queryDiscountDetailByOrder(new OrderCouponDTO(couponIds, orderCourses));
            if(discount != null) {
                order.setDiscountAmount(discount.getDiscountAmount());
                order.setCouponIds(discount.getIds());
            }
        }
        Integer payAmount = totalAmount - order.getDiscountAmount();
        // 5.3.封装其它信息
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setOrderStatus(OrderStatus.NO_PAY);
        order.setRemark(OrderStatus.NO_PAY.getProgressName());
        // 5.4订单号
        order.setId(orderId);
        // 6.封装订单详情
        List<OrderItem> orderItems = new ArrayList<>(courseInfos.size());
        for (CourseSimpleInfoDTO courseInfo : courseInfos) {
            Integer discountValue = discount == null ?
                    0 : discount.getDiscountDetail().getOrDefault(courseInfo.getId(), 0);
            orderItems.add(packageOrderItem(courseInfo, order, discountValue));
        }
        // 7.写入数据库
        saveOrderAndItems(order, orderItems);
        // 8.删除购物车
        CartUserDTO cartUserDTO = new CartUserDTO();
        cartUserDTO.setUserId(userId);
        cartUserDTO.setIds(courseIds);
        mqHelper.send(MqConstants.Exchange.ORDER_EXCHANGE,MqConstants.Key.ORDER_CART_KEY,cartUserDTO);
        // 9.创建订单状态日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setFromStatus(OrderStatus.NO_PAY);
        orderLog.setToStatus(OrderStatus.NO_PAY);
        orderLog.setReason("创建订单");
        mqHelper.send(MqConstants.Exchange.ORDER_EXCHANGE,MqConstants.Key.ORDER_LOG_KEY,orderLog);
        // 10.构建下单结果
        return OrderCreateVO.builder()
                .orderId(orderId)
                .payAmount(payAmount)
                .status(order.getOrderStatus())
                .payOutTime(LocalDateTime.now().plusMinutes(tradeProperties.getPayOrderTTLMinutes()))
                .build();
    }

    private void saveOrderAndItems(Order order, List<OrderItem> orderItems) {
        // 7.1.写订单
        boolean success = save(order);
        if (!success) {
            throw new DbException(TradeErrorInfo.PLACE_ORDER_FAILED);
        }
        // 7.2.写订单详情
        if(orderItems.size() == 1){
            success = orderItemService.save(orderItems.get(0));
        }else {
            success = orderItemService.saveBatch(orderItems);
        }
        if (!success) {
            throw new DbException(TradeErrorInfo.PLACE_ORDER_FAILED);
        }
    }

    private OrderItem packageOrderItem(CourseSimpleInfoDTO courseInfo, Order order, Integer discountValue) {
        OrderItem item = new OrderItem();
        item.setUserId(order.getUserId());
        item.setOrderId(order.getId());
        item.setItemStatus(order.getOrderStatus());
        item.setSpuId(courseInfo.getThirdCateId());
        item.setSkuId(courseInfo.getId());
        item.setItemCover(courseInfo.getCoverUrl());
        item.setItemName(courseInfo.getName());
        item.setValidDuration(courseInfo.getValidDuration());
        item.setPrice(courseInfo.getPrice());
        item.setDiscountAmount(discountValue);
        item.setPayAmount(courseInfo.getPrice() - item.getDiscountAmount());
        return item;
    }

    private List<CourseSimpleInfoDTO> getOnShelfCourse(List<Long> courseIds) {
        // 1.查询课程
        List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
        LocalDateTime now = LocalDateTime.now();
        // 2.判断状态
        for (CourseSimpleInfoDTO courseInfo : courseInfos) {
            // 2.1.检查课程是否上架
            if(CourseStatus.ON_SHELF != courseInfo.getStatus()){
                throw new BizIllegalException(TradeErrorInfo.COURSE_NOT_FOR_SALE);
            }
            // 2.2.检查课程是否过期
            if(courseInfo.getPurchaseEndTime().isBefore(now)){
                throw new BizIllegalException(TradeErrorInfo.COURSE_EXPIRED);
            }
        }
        return courseInfos;
    }

    @Override
    public OrderConfirmVO prePlaceOrder(List<Long> courseIds) {
        // 1.查询课程信息
        List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(courseInfos)) {
            throw new BizIllegalException(TradeErrorInfo.COURSE_NOT_EXISTS);
        }
        List<OrderCourseVO> courses = BeanUtils.copyList(courseInfos, OrderCourseVO.class);
        // 2.计算总价
        int total = courseInfos.stream().mapToInt(CourseSimpleInfoDTO::getPrice).sum();
        // 3.计算折扣
        List<OrderCourseDTO> orderCourses = courseInfos.stream()
                .map(ci -> new OrderCourseDTO().setId(ci.getId()).setCateId(ci.getThirdCateId()).setPrice(ci.getPrice()))
                .collect(Collectors.toList());
        List<CouponDiscountDTO> discountSolution = promotionClient.findDiscountSolution(orderCourses);
        // 4.生成订单id
        String orderId = getIdempotentToken();
        // 5.组织返回
        OrderConfirmVO vo = new OrderConfirmVO();
        vo.setOrderId(orderId);
        vo.setTotalAmount(total);
        vo.setDiscounts(discountSolution);
        vo.setCourses(courses);
        return vo;
    }

    @Override
    public OrderVO queryOrderById(Long id) {
        // 1.查询订单
        Order order = getById(id);
        if (order == null) {
            throw new BadRequestException(ORDER_NOT_EXISTS);
        }
        // 2.查询订单详情
        List<OrderItem> items = orderItemService.queryByOrderId(id);
        // 3.转换VO
        // 3.1.订单
        OrderVO vo = BeanUtils.toBean(order, OrderVO.class);
        // 3.2.订单详情
        List<OrderItemVO> dvs = BeanUtils.copyList(items, OrderItemVO.class, (d, v) -> v.setCanRefund(
                // 订单已经支付，且 退款没有在进行中，标记为可退款状态
                OrderStatus.canRefund(d.getItemStatus()) && !RefundStatus.inProgress(v.getRefundStatus())
        ));
        vo.setDetails(dvs);
        // 3.3.订单进度
        RefundApply refundApply = refundApplyService.getById(id);
        vo.setProgressNodes(orderItemService.packageProgressNodes(order, refundApply));
        // 3.4.优惠明细
        List<String> rules = promotionClient.queryDiscountRules(order.getCouponIds());
        vo.setCouponDesc(String.join("/", rules));
        return vo;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, OrderCancelReason cancelReason) {
        Long userId = UserContext.getUser();
        // 1.查询订单
        Order order = getById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BadRequestException(ORDER_NOT_EXISTS);
        }
        // 2.判断订单状态是否已经取消，幂等判断
        if(OrderStatus.CLOSED == order.getOrderStatus()){
            // 订单已经取消，无需重复操作
            return;
        }
        // 3.判断订单是否未支付，只有未支付订单才可以取消
        if(OrderStatus.NO_PAY != order.getOrderStatus()){
            throw new BizIllegalException(ORDER_ALREADY_FINISH);
        }
        // 4.可以更新订单状态为取消了
        boolean success = lambdaUpdate()
                .set(Order::getOrderStatus, OrderStatus.CLOSED.getValue())
                .set(Order::getRemark, cancelReason.getMsg())
                .set(Order::getCloseTime, LocalDateTime.now())
                .eq(Order::getOrderStatus, OrderStatus.NO_PAY.getValue())
                .eq(Order::getId, orderId)
                .update();
        if (!success) {
            return;
        }
        // 5.更新订单条目的状态
        orderItemService.updateStatusByOrderId(orderId, OrderStatus.CLOSED);

        // 6.退还优惠券
        promotionClient.refundCoupon(order.getCouponIds());
    }

    @Override
    @Transactional
    public void handlePaySuccess(PayResultDTO payResult) {
        // 1.查询订单
        Order order = getById(payResult.getBizOrderId());
        if (order == null) {
            return;
        }
        // 2.更新订单状态
        Order o = new Order();
        o.setId(order.getId());
        o.setOrderStatus(OrderStatus.PAYED);
        o.setPayTime(payResult.getSuccessTime());
        o.setPayChannel(payResult.getPayChannel());
        o.setOrderNo(payResult.getPayOrderNo());
        o.setRemark("用户支付成功");
        updateById(o);
        // 3.更新订单条目
        orderItemService.markDetailSuccessByOrderId(o.getId(), payResult.getPayChannel(), payResult.getSuccessTime());
        // 4.查询订单包含的课程信息
        List<Long> cIds = orderItemService.queryCourseIdsByOrderId(o.getId());
        // 5.发送MQ消息，通知报名成功
        mqHelper.send(
                MqConstants.Exchange.ORDER_EXCHANGE,
                MqConstants.Key.ORDER_PAY_KEY,
                OrderBasicDTO.builder()
                        .orderId(o.getId()).userId(order.getUserId()).courseIds(cIds)
                        .finishTime(o.getPayTime())
                        .build()
        );
    }


    @Override
    public String getIdempotentToken() {
        Long userId = UserContext.getUser();
        return idempotentHelper.getIdempotentToken(userId,"order:");
    }
}
