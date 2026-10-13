package com.pajamas.pay.third;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pj.pay")
public class CommonPayProperties {
    private String notifyHost;

    /**
     * 支付超时时间（分钟）
     */
    private Integer payOrderTTLMinutes = 30;
}
