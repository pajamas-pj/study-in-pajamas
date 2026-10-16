package com.pajamas.trade.handler;


import com.pajamas.trade.service.IOrderService;
import com.pajamas.trade.service.IPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayMessageHandler {

    private final IOrderService orderService;
    private final IPayService payService;

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
//            exchange = @Exchange(name = MqConstants.Exchange.PAY_EXCHANGE, type = ExchangeTypes.TOPIC),
//            key = MqConstants.Key.PAY_SUCCESS
//    ))
//    public void listenPaySuccess(PayResultDTO payResult){
//        log.debug("收到支付成功通知：{}", payResult);
//        orderService.handlePaySuccess(payResult);
//    }
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "trade.delay.order.query", durable = "true"),
//            exchange = @Exchange(name = MqConstants.Exchange.TRADE_DELAY_EXCHANGE, delayed = "true", type = ExchangeTypes.TOPIC),
//            key = MqConstants.Key.ORDER_DELAY_KEY
//    ))
//    public void listenOrderDelayQueryMessage(OrderDelayQueryDTO message){
//        log.debug("收到订单延迟查询通知：{}", message);
//        payService.queryPayResult(message);
//    }
}
