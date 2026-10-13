package com.pajamas.pay.third.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PayCloseResponse {

    @ApiModelProperty("是否成功")
    private boolean success;

    @ApiModelProperty("错误码，请求失败时返回")
    private String code;

    @ApiModelProperty("错误信息，请求失败时返回")
    private String msg;
}
