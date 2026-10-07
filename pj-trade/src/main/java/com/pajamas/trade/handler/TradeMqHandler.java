package com.pajamas.trade.handler;

import com.pajamas.trade.domain.dto.CartUserDTO;
import com.pajamas.trade.domain.po.OrderLog;
import com.pajamas.trade.service.ICartService;
import com.pajamas.trade.service.IOrderLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.pajamas.common.constants.MqConstants.Exchange.ORDER_EXCHANGE;
import static com.pajamas.common.constants.MqConstants.Key.ORDER_CART_KEY;
import static com.pajamas.common.constants.MqConstants.Key.ORDER_LOG_KEY;

@Component
@RequiredArgsConstructor
public class TradeMqHandler {

    private final ICartService cartService;
    private final IOrderLogService orderLogService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "order.cart.queue", durable = "true"),
            exchange = @Exchange(name = ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = ORDER_CART_KEY
    ))
    public void listenCartDeleteMessage(CartUserDTO cartUserDTO){
        cartService.deleteCartByUserAndCourseIds(cartUserDTO);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "order.log.queue", durable = "true"),
            exchange = @Exchange(name = ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = ORDER_LOG_KEY
    ))
    public void createOrderLog(OrderLog orderLog){
        orderLogService.createOrderLog(orderLog);
    }
}
