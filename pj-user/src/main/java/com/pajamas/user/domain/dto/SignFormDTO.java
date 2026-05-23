package com.pajamas.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "注册表单实体")
public class SignFormDTO {

    @ApiModelProperty(value = "用户名", example = "pengjie")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "手机号", example = "15300000000")
    @NotBlank(message = "手机号不能为空")
    private String cellphone;

    @ApiModelProperty(value = "密码", example = "1234", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}