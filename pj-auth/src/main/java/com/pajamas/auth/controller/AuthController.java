package com.pajamas.auth.controller;

import com.pajamas.api.dto.user.LoginFormDTO;
import com.pajamas.auth.domain.vo.LoginVO;
import com.pajamas.auth.service.IAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @ApiOperation("用户登录接口")
    @PostMapping("/login")
    public LoginVO login(@RequestBody LoginFormDTO loginDTO, HttpServletResponse response) {
        return authService.login(loginDTO, response);
    }

    @ApiOperation("刷新令牌接口")
    @PostMapping("/refresh")
    public LoginVO refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshTokens(request, response);
    }

    @ApiOperation("登出接口")
    @PostMapping("/logout")
    public Void logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }
}