package com.pajamas.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.api.dto.user.LoginFormDTO;
import com.pajamas.api.dto.user.LoginUserDTO;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.exceptions.ForbiddenException;
import com.pajamas.user.domain.dto.SignFormDTO;
import com.pajamas.user.domain.po.User;
import com.pajamas.user.enums.UserStatus;
import com.pajamas.user.mapper.UserMapper;
import com.pajamas.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO) {
        // 1.判断登录方式
        Integer type = loginDTO.getType();
        User user = null;
        // 2.用户名和密码登录
        if (type == 1) {
            user = loginByPw(loginDTO);
        }
        // 3.验证码登录
        if (type == 2) {
            user = loginByVerifyCode(loginDTO.getCellphone(), loginDTO.getPassword());
        }
        // 4.错误的登录方式
        if (user == null) {
            throw new BadRequestException("错误的登录方式");
        }
        // 6.封装返回
        LoginUserDTO userDTO = new LoginUserDTO();
        userDTO.setUserId(user.getId());
        userDTO.setRememberMe(loginDTO.getRememberMe());
        return userDTO;
    }

    @Override
    public void sign(SignFormDTO signDTO) {
        // 1. 校验手机号是否已存在
        Integer count = lambdaQuery().eq(User::getCellphone, signDTO.getCellphone()).count();
        if (count > 0) {
            throw new BadRequestException("手机号已注册");
        }
        // 2. 创建用户
        User user = new User();
        user.setUsername(signDTO.getUsername());
        user.setCellphone(signDTO.getCellphone());
        user.setPassword(passwordEncoder.encode(signDTO.getPassword()));
        save(user);
    }

    public User loginByPw(LoginFormDTO loginDTO) {
        // 1.数据校验
        String username = loginDTO.getUsername();
        String cellPhone = loginDTO.getCellphone();
        if (StrUtil.isBlank(username) && StrUtil.isBlank(cellPhone)) {
            throw new BadRequestException("用户名或手机号为空");
        }
        // 2.根据用户名或手机号查询
        User user = lambdaQuery()
                .eq(StrUtil.isNotBlank(username), User::getUsername, username)
                .eq(StrUtil.isNotBlank(cellPhone), User::getCellphone, cellPhone)
                .one();
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        // 4.校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BadRequestException("密码错误");
        }
        return user;
    }
    public User loginByVerifyCode(String phone, String code) {
        // 1.校验验证码
        //TODO
        // codeService.verifyCode(phone, code);
        // 2.根据手机号查询
        User user = lambdaQuery().eq(User::getCellphone, phone).one();
        if (user == null) {
            throw new BadRequestException("手机号不存在");
        }
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        return user;
    }
}
