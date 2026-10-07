package com.pajamas.trade.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.pajamas.trade.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品总金额(分)
     */
    private Integer totalAmount;

    /**
     * 优惠总金额(分)
     */
    private Integer discountAmount;

    /**
     * 实付金额(分)
     */
    private Integer payAmount;

    /**
     * 状态:1-待支付 2-已支付 3-已完成 4-已关闭 5-退款中 6-已退款
     */
    private OrderStatus orderStatus;

    /**
     * 支付方式:1-微信 2-支付宝
     */
    private Integer paymentMethod;

    /**
     * 来源:0-购物车 1-直接购买 2-活动
     */
    private Integer source;

    /**
     * 备注
     */
    private String remark;

    /**
     * 乐观锁
     */
    private Integer version;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 支付渠道
     */
    private String payChannel;

    /**
     * 优惠券id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> couponIds;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
