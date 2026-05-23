package com.pajamas.auth.domain.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String accessToken;
    private Long userId;
}
