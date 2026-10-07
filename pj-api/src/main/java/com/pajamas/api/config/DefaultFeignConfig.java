package com.pajamas.api.config;

import com.pajamas.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.pajamas.common.constants.Constant.*;

@Configuration
@EnableFeignClients(basePackages = "com.pajamas.api.client")
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return template -> {
            // 1. 从 ThreadLocal 中获取当前用户 ID
            Long userId = UserContext.getUser();
            // 2. 如果用户存在，将其放入 Feign 请求头中
            if (userId != null) {
                template.header("user-info", userId.toString());
            }
        };
    }

    @Bean
    public RequestInterceptor requestIdInterceptor(){
        return template -> template
                .header(REQUEST_ID_HEADER, MDC.get(REQUEST_ID_HEADER))
                .header(REQUEST_FROM_HEADER, FEIGN_ORIGIN_NAME);
    }
}
