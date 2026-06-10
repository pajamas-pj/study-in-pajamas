package com.pajamas.api.dto.promotion;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "优惠券TCC请求参数")
public class CouponTccFormDTO {
    @ApiModelProperty("用户优惠券id列表")
    private List<Long> userCouponIds;

    @ApiModelProperty("订单号")
    private String orderNo;
}
