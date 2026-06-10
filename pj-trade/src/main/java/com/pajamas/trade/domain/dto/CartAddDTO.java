package com.pajamas.trade.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "添加购物车表单")
public class CartAddDTO {
    @ApiModelProperty(value = "商品id", required = true, example = "1")
    @NotNull(message = "商品id不能为空")
    private Long skuId;

    @ApiModelProperty(value = "店铺id", example = "0")
    private Long shopId;

    @ApiModelProperty(value = "活动id")
    private Long activityId;

    @ApiModelProperty(value = "数量", example = "1")
    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity = 1;

}
