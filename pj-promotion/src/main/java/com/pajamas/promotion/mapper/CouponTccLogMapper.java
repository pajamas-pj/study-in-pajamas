package com.pajamas.promotion.mapper;

import com.pajamas.promotion.domain.po.CouponTccLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 优惠券TCC事务记录 Mapper 接口
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-08
 */
public interface CouponTccLogMapper extends BaseMapper<CouponTccLog> {

    // 查询单条事务记录 加行锁
    @Select("SELECT * FROM coupon_tcc_log WHERE tx_id = #{txId} AND branch_id = #{branchId} AND user_coupon_id = #{userCouponId} FOR UPDATE")
    CouponTccLog selectForUpdate(@Param("txId") String txId, @Param("branchId") Long branchId, @Param("userCouponId") Long userCouponId);
}
