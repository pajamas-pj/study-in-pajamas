package com.pajamas.api.dto.stock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "批量库存预扣请求参数")
public class StockDeductListFormDTO {
    @ApiModelProperty("商品列表（skuId + quantity）")
    private List<StockItemDTO> items;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("预扣过期时间")
    private LocalDateTime expireTime;
}
