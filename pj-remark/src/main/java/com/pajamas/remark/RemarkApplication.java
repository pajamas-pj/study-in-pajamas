package com.pajamas.remark;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.pajamas.remark.mapper")
@SpringBootApplication
public class RemarkApplication {
    public static void main(String[] args) {
        SpringApplication.run(RemarkApplication.class, args);
    }
}