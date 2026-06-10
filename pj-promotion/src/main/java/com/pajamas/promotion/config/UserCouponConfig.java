package com.pajamas.promotion.config;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.pajamas.promotion.domain.dto.UserCouponContext;
import com.pajamas.promotion.enums.UserCouponEvent;
import com.pajamas.promotion.enums.UserCouponStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserCouponConfig {
    @Bean
    public StateMachine<UserCouponStatus, UserCouponEvent, UserCouponContext> userCouponStateMachine() {
        StateMachineBuilder<UserCouponStatus, UserCouponEvent, UserCouponContext>
                builder = StateMachineBuilderFactory.create();

        //下单锁定优惠券
        builder.externalTransition()
                .from(UserCouponStatus.UNUSED)
                .to(UserCouponStatus.LOCKED)
                .on(UserCouponEvent.LOCK)
                .when(ctx -> true)
                .perform((from, to, event, ctx) -> {
                    //TODO 状态日志
                });

        //完成下单使用优惠券
        builder.externalTransition()
                .from(UserCouponStatus.LOCKED)
                .to(UserCouponStatus.USED)
                .on(UserCouponEvent.USE)
                .when(ctx -> true)
                .perform((from, to, event, ctx) -> {
                    //TODO 状态日志
                });

        //下单异常、取消订单释放锁定优惠券
        builder.externalTransition()
                .from(UserCouponStatus.LOCKED)
                .to(UserCouponStatus.UNUSED)
                .on(UserCouponEvent.UNLOCK)
                .when(ctx -> true)
                .perform((from, to, event, ctx) -> {
                    //TODO 状态日志
                });

        return builder.build("UserCouponStateMachine");
    }

}
