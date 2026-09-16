package com.pajamas.auth.domain.vo;

import lombok.Data;

@Data
public class LoginWxVO {
    private String accessToken;
    private Long userId;
    private String refreshToken;
}
