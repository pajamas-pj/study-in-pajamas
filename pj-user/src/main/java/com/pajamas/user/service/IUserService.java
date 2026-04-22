package com.pajamas.user.service;

import com.pajamas.user.domain.dto.LoginFormDTO;
import com.pajamas.user.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.user.domain.vo.UserLoginVO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-22
 */
public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginDTO);

}
