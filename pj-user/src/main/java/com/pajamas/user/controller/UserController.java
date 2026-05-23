package com.pajamas.user.controller;


import com.pajamas.api.dto.user.LoginFormDTO;
import com.pajamas.api.dto.user.LoginUserDTO;
import com.pajamas.user.domain.dto.SignFormDTO;
import com.pajamas.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @PostMapping("/sign")
    @ApiOperation("用户注册")
    public void sign(@RequestBody @Valid SignFormDTO signDTO) {
        userService.sign(signDTO);
    }

    @ApiOperation("登录查询用户信息")
    @PostMapping("/detail")
    public LoginUserDTO queryUserDetail(@RequestBody LoginFormDTO loginDTO){
        return userService.queryUserDetail(loginDTO);
    }

}
