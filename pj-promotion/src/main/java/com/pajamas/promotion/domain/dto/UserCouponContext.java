package com.pajamas.promotion.domain.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserCouponContext {
    private Long userCouponId;      // 用户优惠券记录ID（主键）
    private Long userId;            // 用户ID
    private Long couponId;          // 优惠券模板ID
    private LocalDateTime operateTime; // 操作时间
}
