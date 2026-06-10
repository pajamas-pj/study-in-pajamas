package com.pajamas.api.client.promotion.fallback;

import com.pajamas.api.client.promotion.CouponTccClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class CouponTccFallback implements FallbackFactory<CouponTccClient> {
    @Override
    public CouponTccClient create(Throwable cause) {
        return null;
    }
}
