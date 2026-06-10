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
 * 购物车
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cart")
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 店铺id 0=平台自营
     */
    private Long shopId;

    /**
     * 总件数
     */
    private Integer totalQuantity;

    /**
     * 商品种数（不同SKU数量）
     */
    private Integer itemCount;

    /**
     * 1-有效 0-失效
     */
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer version;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
