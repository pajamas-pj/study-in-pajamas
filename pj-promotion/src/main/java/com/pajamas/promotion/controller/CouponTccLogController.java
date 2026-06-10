package com.pajamas.promotion.controller;

import com.pajamas.api.dto.promotion.CouponTccFormDTO;
import com.pajamas.promotion.service.ICouponTccLogService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 优惠券TCC事务记录 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-08
 */
@Api(tags = "优惠券TCC事务管理")
@RestController
@RequestMapping("/coupon-tcc")
@RequiredArgsConstructor
public class CouponTccLogController {

    private final ICouponTccLogService couponTccLogService;

    @ApiOperation("批量 Try — 锁定优惠券")
    @PostMapping("/try")
    public boolean batchTry(@RequestBody CouponTccFormDTO dto) {
        return couponTccLogService.batchTry(null, dto.getUserCouponIds(), dto.getOrderNo());
    }

    @ApiOperation("批量 Confirm — 确认核销")
    @PostMapping("/confirm")
    public boolean batchConfirm(@RequestBody BusinessActionContext context) {
        return couponTccLogService.batchConfirm(context);
    }

    @ApiOperation("批量 Cancel — 回滚解锁")
    @PostMapping("/cancel")
    public boolean batchCancel(@RequestBody BusinessActionContext context) {
        return couponTccLogService.batchCancel(context);
    }
}
