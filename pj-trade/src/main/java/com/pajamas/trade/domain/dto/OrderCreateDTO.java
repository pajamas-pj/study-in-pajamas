package com.pajamas.trade.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(description = "创建订单表单")
public class OrderCreateDTO {

    @ApiModelProperty(value = "幂等token", required = true)
    @NotEmpty(message = "幂等token不能为空")
    private String idempotentToken;

    @ApiModelProperty(value = "店铺id", example = "0")
    private Long shopId;

    @ApiModelProperty("来源:0-购物车 1-直接购买")
    private Integer source;

    @ApiModelProperty("买家备注")
    private String remark;

    @ApiModelProperty(value = "商品列表", required = true)
    @NotEmpty(message = "商品列表不能为空")
    private List<OrderItemDTO> items;

    @ApiModelProperty("优惠券id列表")
    private List<Long> couponIds;
}
