package com.pajamas.user.controller;


import com.pajamas.user.domain.dto.LoginFormDTO;
import com.pajamas.user.domain.vo.UserLoginVO;
import com.pajamas.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-22
 */
@RestController
@RequestMapping("/users")
@Api(tags = "用户相关接口")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @ApiOperation("用户登录接口")
    @PostMapping("/login")
    public UserLoginVO login(@RequestBody @Validated LoginFormDTO loginFormDTO){
        return userService.login(loginFormDTO);
    }

}
