package com.pajamas.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.api.client.course.CourseClient;
import com.pajamas.api.client.promotion.CouponTccClient;
import com.pajamas.api.client.promotion.PromotionClient;
import com.pajamas.api.client.stock.StockDeductListClient;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.api.dto.promotion.CouponDiscountDTO;
import com.pajamas.api.dto.promotion.CouponTccFormDTO;
import com.pajamas.api.dto.promotion.OrderCouponDTO;
import com.pajamas.api.dto.promotion.OrderCourseDTO;
import com.pajamas.api.dto.stock.StockDeductListFormDTO;
import com.pajamas.api.dto.stock.StockItemDTO;
import com.pajamas.common.exceptions.BizIllegalException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.trade.constants.TradeErrorInfo;
import com.pajamas.trade.domain.dto.OrderCreateDTO;
import com.pajamas.trade.domain.dto.OrderItemDTO;
import com.pajamas.trade.domain.po.CartItem;
import com.pajamas.trade.domain.po.Order;
import com.pajamas.trade.domain.po.OrderItem;
import com.pajamas.trade.domain.po.OrderLog;
import com.pajamas.trade.domain.vo.OrderConfirmVO;
import com.pajamas.trade.domain.vo.OrderCourseVO;
import com.pajamas.trade.domain.vo.OrderCreateVO;
import com.pajamas.trade.mapper.OrderMapper;
import com.pajamas.trade.service.ICartItemService;
import com.pajamas.trade.service.IOrderItemService;
import com.pajamas.trade.service.IOrderLogService;
import com.pajamas.trade.service.IOrderService;
import com.pajamas.trade.utils.RedisIdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService{

    private final PromotionClient promotionClient;
    private final CourseClient courseClient;
    private final CouponTccClient couponTccClient;
    private final StockDeductListClient stockDeductListClient;

    private final IOrderItemService orderItemService;
    private final IOrderLogService orderLogService;
    private final ICartItemService cartItemService;


    private final StringRedisTemplate stringRedisTemplate;
    private final RedisIdWorker redisIdWorker;
    private static final DefaultRedisScript<Long> IDEMPOTENT_SCRIPT = new DefaultRedisScript<>();

    @PostConstruct
    public void init() {
        IDEMPOTENT_SCRIPT.setLocation(new ClassPathResource("lua/idempotent.lua"));
        IDEMPOTENT_SCRIPT.setResultType(Long.class);
    }

    @Override
    @GlobalTransactional
    public OrderCreateVO createOrder(OrderCreateDTO dto) {
        //1.幂等校验
        //1.1redisToken先斩后奏
        Long userId = UserContext.getUser();
        String key = "idempotent:" + userId + ":" + dto.getIdempotentToken();
        Long result = stringRedisTemplate.execute(IDEMPOTENT_SCRIPT, List.of(key));
        if (result == null || result == 0) {
            throw new BizIllegalException("订单已提交，请勿重复操作");
        }
        //2.TCC库存冻结
        //2.1生成订单号
        String orderNo = String.valueOf(redisIdWorker.nextId("order"));
        //2.2订单关闭时间15min
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(15);
        //2.3收集订单商品项
        List<OrderItemDTO> items = dto.getItems();
        if(CollUtils.isEmpty(items)){
            throw new BizIllegalException("订单商品不能为空");
        }
        //2.4转化库存DTO
        List<StockItemDTO> stockItems = BeanUtils.copyList(items, StockItemDTO.class);
        StockDeductListFormDTO formDTO = new StockDeductListFormDTO();
        formDTO.setItems(stockItems);
        formDTO.setOrderNo(orderNo);
        formDTO.setExpireTime(expireTime);
        //2.5调用批量库存TCC
        boolean isTCC = stockDeductListClient.batchTry(formDTO);
        if (!isTCC) {
            throw new BizIllegalException("库存不足");
        }
        //6锁定优惠券
        //6.1构建优惠券TCC参数
        CouponTccFormDTO couponTccFormDTO = new CouponTccFormDTO();
        couponTccFormDTO.setUserCouponIds(dto.getCouponIds());
        couponTccFormDTO.setOrderNo(orderNo);
        boolean isCoupon = couponTccClient.batchTry(couponTccFormDTO);
        if (!isCoupon) {
            throw new BizIllegalException("优惠券无效");
        }
        //3调用课程服务获取SPU（三级分类id）和实时价格
        //3.1收集课程id
        List<Long> courseIds = items.stream().map(OrderItemDTO::getSkuId).collect(Collectors.toList());
        //3.2调用课程服务
        List<CourseSimpleInfoDTO> courses = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(courses)) {
            throw new BizIllegalException("课程信息获取失败");
        }
        //3.3收集成map
        Map<Long, CourseSimpleInfoDTO> courseMap = courses.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        //4.重算价格
        //4.1原价总计
        long totalAmount = 0L;
        List<OrderItem> orderItems = new ArrayList<>(items.size());
        for (OrderItemDTO item : items) {
            //4.2获取课程信息
            CourseSimpleInfoDTO course = courseMap.get(item.getSkuId());
            if (course == null) {
                throw new BizIllegalException("课程不存在");
            }
            //4.3计算商品项总价
            long itemTotal = (long) course.getPrice() * item.getQuantity();
            totalAmount += itemTotal;
            //4.4构建订单商品项
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setShopId(dto.getShopId());

            orderItem.setSpuId(course.getThirdCateId());
            orderItem.setItemName(course.getName());
            orderItem.setItemCover(course.getCoverUrl());
            orderItem.setPrice(course.getPrice().longValue());

            orderItem.setSkuId(item.getSkuId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setActivityId(item.getActivityId());

            orderItems.add(orderItem);
        }
        //5.调用优惠券服务计算优惠
        //5.1收集用户券id
        List<Long> couponIds = dto.getCouponIds();
        int discountAmount = 0;
        long payAmount = totalAmount;
        //5.2如果用券，调用优惠券服务计算优惠
        if (CollUtils.isNotEmpty(couponIds)) {
            //5.3构建优惠券调用DTO
            List<OrderCourseDTO> orderCourses = BeanUtils.copyList(courses, OrderCourseDTO.class,
                    (course, o) -> o.setCateId(course.getThirdCateId()));
            OrderCouponDTO orderCouponDTO = new OrderCouponDTO();
            orderCouponDTO.setUserCouponIds(couponIds);
            orderCouponDTO.setCourseList(orderCourses);
            CouponDiscountDTO discountDTO = promotionClient.queryDiscountDetailByOrder(orderCouponDTO);
            if (discountDTO == null ) {
                throw new BizIllegalException("优惠券信息获取失败");
            }
            discountAmount = discountDTO.getDiscountAmount();
            //5.4计算总支付金额
            payAmount = totalAmount - discountAmount < 0 ? 0 : totalAmount - discountAmount;
            //5.5分摊优惠到每个商品项
            Map<Long, Integer> discountDetail = discountDTO.getDiscountDetail();
            for (OrderItem orderItem : orderItems) {
                int itemDiscount = discountDetail != null
                        ? discountDetail.getOrDefault(orderItem.getSkuId(), 0) : 0;
                long itemTotal = orderItem.getPrice() * orderItem.getQuantity();
                long itemPay = itemTotal - itemDiscount < 0 ? 0 : itemTotal - itemDiscount;
                orderItem.setDiscountAmount((long) itemDiscount);
                orderItem.setPayAmount(itemPay);
            }
        }
        //6.TCC锁定优惠券
        if (CollUtils.isNotEmpty(couponIds)) {
            CouponTccFormDTO tccForm = new CouponTccFormDTO();
            tccForm.setUserCouponIds(couponIds);
            tccForm.setOrderNo(orderNo);
            boolean locked = couponTccClient.batchTry(tccForm);
            if (!locked) {
                throw new BizIllegalException("优惠券锁定失败");
            }
        }
        //7.保存订单和订单商品项
        //7.1保存订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCloseTime(expireTime);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount((long) discountAmount);
        order.setPayAmount(payAmount);
        order.setShopId(dto.getShopId());
        order.setSource(dto.getSource());
        order.setRemark(dto.getRemark());
        boolean save1 = save(order);
        if (!save1) {
            throw new BizIllegalException("订单保存失败");
        }
        //7.2保存订单商品项
        Long orderId = order.getId();
        orderItems.forEach(item -> item.setOrderId(orderId));
        boolean save2 = orderItemService.saveBatch(orderItems);
        if (!save2) {
            throw new BizIllegalException("订单商品项保存失败");
        }
        //7.3保存订单日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setToStatus(1);
        orderLog.setOperator("USER");
        orderLog.setReason("创建订单");
        boolean save3 = orderLogService.save(orderLog);
        if (!save3) {
            throw new BizIllegalException("订单日志保存失败");
        }
        //8.清除购物车对应商品
        if (dto.getSource() != null && dto.getSource() == 0) {
            cartItemService.lambdaUpdate()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getSkuId, courseIds)
                    .remove();
        }
        //9.返回结果
        OrderCreateVO vo = new OrderCreateVO();
        vo.setOrderNo(orderNo);
        return vo;
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
    public String getIdempotentToken() {
        Long userId = UserContext.getUser();
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = "idempotent:" + userId + ":" + token;
        stringRedisTemplate.opsForValue().set(key, "1", 30, TimeUnit.MINUTES);
        return token;
    }
}
