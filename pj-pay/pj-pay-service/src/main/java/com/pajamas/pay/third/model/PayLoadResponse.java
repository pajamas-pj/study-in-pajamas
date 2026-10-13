package com.pajamas.pay.third.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayLoadResponse {

    /**
     * 是否成功
     */
    private boolean success;

    @ApiModelProperty("用户唯一标识")
    private String openId;

    @ApiModelProperty("会话密钥")
    @JsonProperty("session_key")
    private String sessionKey;

    @ApiModelProperty("用户在开放平台的唯一标识符（绑定开放平台才有）")
    private String unionId;

    @ApiModelProperty("错误码，请求失败时返回")
    private Integer code;

    @ApiModelProperty("错误信息，请求失败时返回")
    private String msg;


}
