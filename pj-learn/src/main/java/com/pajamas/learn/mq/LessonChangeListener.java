package com.pajamas.learn.mq;


import com.pajamas.api.dto.trade.OrderBasicDTO;
import com.pajamas.common.constants.MqConstants;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.learn.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LessonChangeListener {

    private final ILearningLessonService lessonService;
    //采取RabbitMQ的监听模式，监听课程表信息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "learning.lesson.pay.queue",durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_EXCHANGE ,type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_PAY_KEY
    ))
    public void listenLessonPay(OrderBasicDTO order){
        if(order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())){
            log.error("消息为空");
            return;
        }
        log.debug("处理订单支付消息{}{}{}",order.getOrderId(),order.getUserId(),order.getCourseIds());
        lessonService.addUserLessons(order.getUserId(), order.getCourseIds());
     }
}
