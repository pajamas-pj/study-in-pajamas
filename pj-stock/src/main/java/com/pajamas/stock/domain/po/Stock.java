package com.pajamas.stock.domain.po;

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
 * 商品库存
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("stock")
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU id
     */
    @TableId(value = "sku_id", type = IdType.INPUT)
    private Long skuId;

    /**
     * 总库存
     */
    private Integer quantity;

    /**
     * 已售
     */
    private Integer sold;

    /**
     * 预扣(未支付)
     */
    private Integer frozen;

    /**
     * 乐观锁
     */
    private Integer version;

    private LocalDateTime updateTime;


}
