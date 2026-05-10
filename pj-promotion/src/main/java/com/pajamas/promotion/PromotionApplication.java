package com.pajamas.promotion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync()
@EnableFeignClients(basePackages = "com.pajamas.api.client")
@MapperScan("com.pajamas.promotion.mapper")
@SpringBootApplication
public class PromotionApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromotionApplication.class, args);
    }
}