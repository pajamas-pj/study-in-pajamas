package com.pajamas.api.dto.user;

import lombok.Data;

@Data
public class LoginUserDTO {
    private Long userId;
    private Long roleId;
    private Boolean rememberMe;
}
