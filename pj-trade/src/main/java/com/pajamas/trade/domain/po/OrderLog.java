package com.pajamas.trade.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.pajamas.trade.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单状态日志
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_log")
public class OrderLog implements Serializable {

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
     * 原状态
     */
    private OrderStatus fromStatus;

    /**
     * 新状态
     */
    private OrderStatus toStatus;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作原因
     */
    private String reason;

    private LocalDateTime createTime;


}
