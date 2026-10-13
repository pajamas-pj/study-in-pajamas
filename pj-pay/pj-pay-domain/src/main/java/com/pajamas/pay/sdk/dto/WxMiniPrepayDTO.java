package com.pajamas.pay.sdk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "小程序调起支付参数")
public class WxMiniPrepayDTO {

    @ApiModelProperty("时间戳（秒级）")
    private String timeStamp;

    @ApiModelProperty("随机字符串")
    private String nonceStr;

    @ApiModelProperty("订单详情扩展字符串，格式：prepay_id=xxx")
    @JsonProperty("package")
    private String packageValue;   // 注意：package 是 Java 关键字，字段名不能直接用

    @ApiModelProperty("签名方式，固定 RSA")
    private String signType;

    @ApiModelProperty("签名值")
    private String paySign;

}