package com.pajamas.api.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("登录用户详情")
public class LoginUserDTO {
    @ApiModelProperty(value = "用户id", example = "1")
    private Long userId;
    @ApiModelProperty(value = "角色id", example = "1")
    private Long roleId;
    @ApiModelProperty(value = "7天免密登录", example = "true")
    private Boolean rememberMe;
}
