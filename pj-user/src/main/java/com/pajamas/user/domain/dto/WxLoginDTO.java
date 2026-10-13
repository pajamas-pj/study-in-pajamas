package com.pajamas.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "微信小程序登录参数")
public class WxLoginDTO {

    @ApiModelProperty(value = "wx.login() 返回的 code", example = "081K8q000xxxxx")
    private String code;
}
