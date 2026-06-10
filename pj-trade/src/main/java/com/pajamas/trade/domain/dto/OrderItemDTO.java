package com.pajamas.trade.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "订单商品信息")
public  class OrderItemDTO {
    @ApiModelProperty(value = "课程id(SKU)", required = true)
    @NotNull(message = "课程id不能为空")
    private Long skuId;

    @ApiModelProperty(value = "数量", example = "1")
    private Integer quantity = 1;

    @ApiModelProperty("活动id")
    private Long activityId;
}