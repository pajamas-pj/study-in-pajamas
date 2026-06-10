package com.pajamas.promotion.controller;


import com.pajamas.common.domain.PageDTO;
import com.pajamas.promotion.domain.dto.CouponFormDTO;
import com.pajamas.promotion.domain.dto.CouponIssueFormDTO;
import com.pajamas.promotion.domain.query.CouponQuery;
import com.pajamas.promotion.domain.vo.CouponPageVO;
import com.pajamas.promotion.domain.vo.CouponVO;
import com.pajamas.promotion.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
@Api(tags = "优惠券相关接口")
public class CouponController {

    private final ICouponService couponService;

    @ApiOperation("新增优惠券接口")
    @PostMapping
    public void saveCoupon(@RequestBody @Valid CouponFormDTO dto) {
        couponService.saveCoupon(dto);
    }
    @ApiOperation("分页查询优惠券接口")
    @GetMapping("/page")
    public PageDTO<CouponPageVO> queryCouponByPage(CouponQuery query){
        return couponService.queryCouponByPage(query);
    }

    @ApiOperation("发放优惠券接口")
    @PutMapping("/issue")
    public void beginIssue(@RequestBody @Valid CouponIssueFormDTO dto) {
        couponService.beginIssue(dto);
    }

    @ApiOperation("查询发放中的优惠券列表")
    @GetMapping("/list")
    public List<CouponVO> queryIssuingCoupons(){
        return couponService.queryIssuingCoupons();
    }
}
