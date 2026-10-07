package com.pajamas.trade.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "用户商品MQ消息实体")
public class CartUserDTO {

    @ApiModelProperty("用户id")
    Long userId;

    @ApiModelProperty("商品项id集合")
    private List<Long> ids;
}
