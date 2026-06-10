package com.pajamas.api.client.promotion;

import com.pajamas.api.client.promotion.fallback.CouponTccFallback;
import com.pajamas.api.dto.promotion.CouponTccFormDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "promotion-service", contextId = "coupon-tcc", path = "/coupon-tcc", fallbackFactory = CouponTccFallback.class)
public interface CouponTccClient {

    @ApiOperation("TCC 批量锁定优惠券")
    @PostMapping("/try")
    boolean batchTry(@RequestBody CouponTccFormDTO dto);
}
