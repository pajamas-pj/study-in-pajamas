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
 * 退款单
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("refund_order")
public class RefundOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 原订单id
     */
    private Long orderId;

    /**
     * 明细id(null=整单退款)
     */
    private Long orderItemId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 退款金额(分)
     */
    private Long refundAmount;

    /**
     * 状态:1-待处理 2-已同意 3-已退款 4-已拒绝
     */
    private Integer refundStatus;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 退款完成时间
     */
    private LocalDateTime finishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
