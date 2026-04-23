package com.pajamas.api.config;

import com.pajamas.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;


public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1. 从 ThreadLocal 中获取当前用户 ID
                Long userId = UserContext.getUser();
                // 2. 如果用户存在，将其放入 Feign 请求头中
                if (userId != null) {
                    template.header("user-info", userId.toString());
                }
            }
        };
    }

}
