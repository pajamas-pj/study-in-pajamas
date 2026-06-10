package com.pajamas.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.pajamas.promotion.enums.CouponTccStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 优惠券TCC事务记录
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("coupon_tcc_log")
public class CouponTccLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 全局事务ID
     */
    private String txId;

    /**
     * 分支事务ID
     */
    private Long branchId;

    /**
     * 用户券ID
     */
    private Long couponId;

    /**
     * 1-TRIED 2-CONFIRMED 3-CANCELLED
     */
    private CouponTccStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
