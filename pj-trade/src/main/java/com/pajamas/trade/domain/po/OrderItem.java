package com.pajamas.trade.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单明细
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 用户id(冗余,分片)
     */
    private Long userId;

    /**
     * 店铺id(冗余)
     */
    private Long shopId;

    /**
     * SPU id
     */
    private Long spuId;

    /**
     * SKU id
     */
    private Long skuId;

    /**
     * 商品名称(快照)
     */
    private String itemName;

    /**
     * 商品封面(快照)
     */
    private String itemCover;

    /**
     * 原价(分)
     */
    private Long price;

    /**
     * 优惠分摊(分)
     */
    private Long discountAmount;

    /**
     * 实付小计(分)
     */
    private Long payAmount;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 状态:1-待支付 2-已支付 3-已完成 4-已关闭 5-退款中 6-已退款
     */
    private Integer itemStatus;

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 扩展字段
     */
    private String extend;

    private LocalDateTime createTime;


}
