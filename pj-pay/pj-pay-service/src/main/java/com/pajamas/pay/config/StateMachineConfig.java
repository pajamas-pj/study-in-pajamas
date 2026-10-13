package com.pajamas.pay.config;


import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.pajamas.pay.domain.enums.PayOrderEvent;
import com.pajamas.pay.sdk.dto.PayOrderContext;
import com.pajamas.pay.third.model.PayStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StateMachineConfig {
    @Bean
    public StateMachine<PayStatus, PayOrderEvent, PayOrderContext> payOrderStateMachine() {
        StateMachineBuilder<PayStatus, PayOrderEvent, PayOrderContext>
                builder = StateMachineBuilderFactory.create();

        //超时关闭订单
        builder.externalTransition()
                .from(PayStatus.WAIT_BUYER_PAY)
                .to(PayStatus.TRADE_CLOSED)
                .on(PayOrderEvent.CLOSE_ORDER)
                .when(ctx -> true)
                .perform((from, to, event, ctx) -> {
                    //TODO 状态日志
                });

        //支付成功
        builder.externalTransition()
                .from(PayStatus.WAIT_BUYER_PAY)
                .to(PayStatus.TRADE_SUCCESS)
                .on(PayOrderEvent.PAY_SUCCESS)
                .when(ctx -> true)
                .perform((from, to, event, ctx) -> {
                    //TODO 状态日志
                });



        return builder.build("PayOrderStateMachine");
    }
}
