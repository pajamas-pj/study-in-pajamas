package com.pajamas.api.dto.stock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "库存预扣请求参数")
public class StockTryDeductDTO {
    @ApiModelProperty("课程ID（SKU）")
    private Long skuId;

    @ApiModelProperty("预扣数量")
    private Integer quantity;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("预扣过期时间")
    private LocalDateTime expireTime;
}
