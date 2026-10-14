package com.pajamas.trade.domain.vo;

import com.pajamas.trade.enums.OrderStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "创建订单返回")
@Builder
public class OrderCreateVO {
    @ApiModelProperty("订单号")
    private Long orderId;
    @ApiModelProperty("支付金额")
    private Integer payAmount;
    @ApiModelProperty("待支付的订单超时时间")
    private LocalDateTime payOutTime;
    @ApiModelProperty("订单状态，1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名, 6:申请退款")
    private OrderStatus status;
}
