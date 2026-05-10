package com.pajamas.promotion.service;

import com.pajamas.common.domain.PageDTO;
import com.pajamas.promotion.domain.dto.CouponFormDTO;
import com.pajamas.promotion.domain.dto.CouponIssueFormDTO;
import com.pajamas.promotion.domain.po.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.promotion.domain.query.CouponQuery;
import com.pajamas.promotion.domain.vo.CouponPageVO;
import com.pajamas.promotion.domain.vo.CouponVO;

import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-06
 */
public interface ICouponService extends IService<Coupon> {

    void saveCoupon(CouponFormDTO dto);

    PageDTO<CouponPageVO> queryCouponByPage(CouponQuery query);

    void beginIssue(CouponIssueFormDTO dto);

    List<CouponVO> queryIssuingCoupons();
}
