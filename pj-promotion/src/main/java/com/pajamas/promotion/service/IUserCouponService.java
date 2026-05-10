package com.pajamas.promotion.service;

import com.pajamas.promotion.domain.dto.UserCouponDTO;
import com.pajamas.promotion.domain.po.UserCoupon;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-06
 */
public interface IUserCouponService extends IService<UserCoupon> {

    void receiveCoupon(Long couponId);

    @Transactional
    void checkAndCreateUserCoupon(UserCouponDTO uc);

    void exchangeCoupon(String code);
}
