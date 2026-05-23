package com.pajamas.user.service;

import com.pajamas.api.dto.user.LoginFormDTO;
import com.pajamas.api.dto.user.LoginUserDTO;
import com.pajamas.user.domain.dto.SignFormDTO;
import com.pajamas.user.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-22
 */
public interface IUserService extends IService<User> {

    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO);

    void sign(SignFormDTO signDTO);
}
