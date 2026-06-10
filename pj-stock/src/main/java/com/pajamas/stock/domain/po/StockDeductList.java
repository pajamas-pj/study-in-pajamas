package com.pajamas.stock.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.pajamas.stock.enums.TCCStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * TCC事务记录List
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("stock_deduct_list")
public class StockDeductList implements Serializable {

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
     * 订单号用于查询
     */
    private String orderNo;

    /**
     * 商品信息JSON skuId quantity
     */
    private String itemsJson;

    /**
     * 1-TRIED 2-CONFIRMED 3-CANCELLED
     */
    private TCCStatus status;

    /**
     * 过期时间(订单超时时间)
     */
    private LocalDateTime expireTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
