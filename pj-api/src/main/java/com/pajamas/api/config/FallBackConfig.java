package com.pajamas.api.config;

import com.pajamas.api.client.learning.fallback.LearningClientFallback;
import com.pajamas.api.client.promotion.fallback.PromotionClientFallback;
import com.pajamas.api.client.remark.fallback.RemarkClientFallback;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.pajamas.api.client")
public class FallBackConfig {

    @Bean
    public LearningClientFallback learningClientFallback() {
        return new LearningClientFallback();
    }
    @Bean
    public RemarkClientFallback remarkClientFallback() {
        return new RemarkClientFallback();
    }
    @Bean
    public PromotionClientFallback promotionClientFallback(){return new PromotionClientFallback();}
}
