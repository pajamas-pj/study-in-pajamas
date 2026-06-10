package com.pajamas.stock.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "库存")
public class StockVO {
    @ApiModelProperty("SKU id")
    private Long skuId;

    @ApiModelProperty("总库存")
    private Integer quantity;

    @ApiModelProperty("已售")
    private Integer sold;

    @ApiModelProperty("预扣(未支付)")
    private Integer frozen;

    @ApiModelProperty("可售库存")
    private Integer available;
}
