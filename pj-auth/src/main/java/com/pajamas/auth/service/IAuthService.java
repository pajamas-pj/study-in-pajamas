package com.pajamas.auth.service;


import com.pajamas.api.dto.user.LoginFormDTO;
import com.pajamas.auth.domain.vo.LoginVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IAuthService {
    LoginVO login(LoginFormDTO loginDTO, HttpServletResponse response);

    LoginVO refreshTokens(HttpServletRequest request, HttpServletResponse response);

    Void logout(HttpServletRequest request, HttpServletResponse response);
}
