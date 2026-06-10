package com.pajamas.trade.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "购物车项信息")
public class CartItemVO {
    @ApiModelProperty("购物车项id")
    private Long id;

    @ApiModelProperty("商品分类id（SPU）")
    private Long spuId;

    @ApiModelProperty("课程id（SKU）")
    private Long skuId;

    @ApiModelProperty("活动id")
    private Long activityId;

    @ApiModelProperty("数量")
    private Integer quantity;

    @ApiModelProperty("选中 1-选中 0-未选")
    private Integer checked;

    @ApiModelProperty("状态 1-正常 2-已失效")
    private Integer status;

    @ApiModelProperty("商品名称")
    private String courseName;

    @ApiModelProperty("商品封面")
    private String coverUrl;

    @ApiModelProperty("商品实时价格，分")
    private Integer realPrice;

    @ApiModelProperty("是否过期/已下架")
    private Boolean expired;
}
