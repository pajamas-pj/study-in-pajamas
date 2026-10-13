package com.pajamas.user.controller;


import com.pajamas.user.domain.dto.WxLoginDTO;
import com.pajamas.user.service.IUserThirdAccountService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户第三方账号关联 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-10-09
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user-third-account")
public class UserThirdAccountController {

    private final IUserThirdAccountService userThirdAccountService;
    @ApiOperation("保存根据wx.login的code用户openId")
    @PostMapping("/wx")
    public void saveWxMiniOpenId(WxLoginDTO wxLoginDTO){
        userThirdAccountService.saveWxOpenId(wxLoginDTO);
    }

    @ApiOperation("查询根据wx.login的code用户openId")
    @GetMapping("/wx")
    public String queryWxMiniOpenIdByUserId(Long userId){
        return userThirdAccountService.queryWxMiniOpenIdByUserId(userId);
    }
}
