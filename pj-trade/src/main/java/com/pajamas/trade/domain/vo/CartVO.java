package com.pajamas.trade.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@ApiModel(description = "购物车信息")
public class CartVO {
    @ApiModelProperty("购物车id")
    private Long id;

    @ApiModelProperty("总件数")
    private Integer totalQuantity;

    @ApiModelProperty("商品种数")
    private Integer itemCount;

    @ApiModelProperty("购物车项列表")
    private List<CartItemVO> items;
}
