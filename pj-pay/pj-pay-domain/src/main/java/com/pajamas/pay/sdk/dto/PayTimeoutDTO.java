package com.pajamas.pay.sdk.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class PayTimeoutDTO {

    @ApiModelProperty("支付单id")
    private Long payOrderId;
    @ApiModelProperty("支付单号")
    private Long payOrderNo;
    @ApiModelProperty("支付超时时间")
    private LocalDateTime payOverTime;
    @ApiModelProperty("支付渠道")
    private String payChannelCode;
    @ApiModelProperty("支付方式: 1-h5；2-小程序；3-公众号；4-扫码")
    @NotNull(message = "支付方式不能为空")
    private Integer payType;
}