package com.pajamas.trade.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
@ApiModel("购物车项修改")
public class CartItemUpdateDTO {

    @ApiModelProperty("购物车项id集合（批量修改选中状态时使用，单个修改走路径参数）")
    private List<Long> ids;

    @ApiModelProperty("选中状态 1-选中 0-未选")
    private Integer checked;

    @Min(1)
    @ApiModelProperty("数量")
    private Integer quantity;
}