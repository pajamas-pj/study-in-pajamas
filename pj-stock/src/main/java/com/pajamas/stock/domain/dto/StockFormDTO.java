package com.pajamas.stock.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "库存表单")
public class StockFormDTO {
    @ApiModelProperty("SKU id")
    private Long skuId;

    @ApiModelProperty("总库存")
    private Integer quantity;
}
