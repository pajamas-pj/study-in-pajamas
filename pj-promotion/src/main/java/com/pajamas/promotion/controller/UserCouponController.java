package com.pajamas.promotion.controller;


import com.pajamas.api.dto.promotion.CouponDiscountDTO;
import com.pajamas.api.dto.promotion.OrderCouponDTO;
import com.pajamas.api.dto.promotion.OrderCourseDTO;
import com.pajamas.promotion.service.IDiscountService;
import com.pajamas.promotion.service.IUserCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user-coupons")
@Api(tags = "用户券相关接口")
public class UserCouponController {

    private final IUserCouponService userCouponService;

    private final IDiscountService discountService;

    @ApiOperation("领取优惠券接口")
    @PostMapping("/{couponId}/receive")
    public void receiveCoupon(@PathVariable("couponId") Long couponId) {
        userCouponService.receiveCoupon(couponId);
    }
    @ApiOperation("兑换码兑换优惠券接口")
    @PostMapping("/{code}/exchange")
    public void exchangeCoupon(@PathVariable("code") String code){
        userCouponService.exchangeCoupon(code);
    }
    @ApiOperation("查询我的优惠券可用方案")
    @PostMapping("/available")
    public List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderCourseDTO> orderCourses){
        return discountService.findDiscountSolution(orderCourses);
    }
    @ApiOperation("根据券方案计算订单优惠明细")
    @PostMapping("/discount")
    public CouponDiscountDTO queryDiscountDetailByOrder(@RequestBody OrderCouponDTO orderCouponDTO){
        return discountService.queryDiscountDetailByOrder(orderCouponDTO);
    }

    @ApiOperation("查询优惠券规则")
    @GetMapping("/rules")
    public List<String> queryDiscountRules(
            @ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds){
        return userCouponService.queryDiscountRules(userCouponIds);
    }
}