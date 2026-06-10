package com.pajamas.api.dto.stock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品信息")
public class StockItemDTO {
    @ApiModelProperty("课程ID（SKU）")
    private Long skuId;

    @ApiModelProperty("预扣数量")
    private Integer quantity;
}
