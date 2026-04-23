package com.pajamas.api.config;

import com.pajamas.api.client.learning.fallback.LearningClientFallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallBackConfig {

    @Bean
    public LearningClientFallback learningClientFallback() {
        return new LearningClientFallback();
    }
}
