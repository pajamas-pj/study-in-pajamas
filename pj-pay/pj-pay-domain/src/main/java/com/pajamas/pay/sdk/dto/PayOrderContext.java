package com.pajamas.pay.sdk.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PayOrderContext {
    private Long bizOrderNo;
    private Long payOrderNo;
    private LocalDateTime operateTime; // 操作时间
}