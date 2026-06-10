package com.pajamas.trade.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "创建订单返回")
public class OrderCreateVO {

    @ApiModelProperty("订单号")
    private String orderNo;
}
