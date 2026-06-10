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
 * 购物车项
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cart_item")
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 购物车id
     */
    private Long cartId;

    /**
     * 用户id（冗余，用于分库分表）
     */
    private Long userId;

    /**
     * 店铺id（冗余）
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
     * 活动id
     */
    private Long activityId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 加购时单价,分，仅展示参考，结算价实时查询
     */
    private Integer priceAtAdd;

    /**
     * 选中 1-选中 0-未选
     */
    private Integer checked;

    /**
     * 1-正常 2-已失效(商品删除/下架)
     */
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
