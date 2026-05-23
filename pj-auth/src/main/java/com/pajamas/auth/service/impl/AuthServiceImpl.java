package com.pajamas.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.pajamas.api.client.user.UserClient;


import com.pajamas.api.dto.user.LoginFormDTO;
import com.pajamas.api.dto.user.LoginUserDTO;
import com.pajamas.auth.config.JwtProperties;
import com.pajamas.auth.domain.vo.LoginVO;
import com.pajamas.auth.service.IAuthService;
import com.pajamas.auth.utils.JwtTool;
import com.pajamas.common.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.pajamas.auth.utils.RedisConstants.LOGIN_USER_KEY;
/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserClient userClient;  // Feign 客户端，调用 user 服务

    @Override
    public LoginVO login(LoginFormDTO loginDTO, HttpServletResponse response) {
        //1.调用openfeign根据用户名或手机号查询用户并校验
        LoginUserDTO user = userClient.queryUserDetail(loginDTO);
        Assert.notNull(user, "用户不存在");
        //TODO 2.是否记住功能

        // 3.生成accessToken
        String accessToken = jwtTool.createToken(user.getUserId(), jwtProperties.getAccessTokenTTL());
        // 4.生成refreshToken并存入redis
        String refreshToken = jwtTool.createToken(user.getUserId(), jwtProperties.getRefreshTokenTTL());
        // 5.1拼接key
        String key  = LOGIN_USER_KEY + refreshToken;
        // 5.2构建用户数据map
        HashMap<Object, Object> userMap = new HashMap<>();
        userMap.put("userId", String.valueOf(user.getUserId()));
        // 5.3存入redis
        stringRedisTemplate.opsForHash().putAll(key, userMap);
        stringRedisTemplate.expire(key, jwtProperties.getRefreshTokenTTL());
        // 6.封装VO
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getUserId());
        vo.setAccessToken(accessToken);
        // 7.创建 HttpOnly
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)           // 防止 XSS 攻击
                .secure(true)             // 生产环境必须 true（HTTPS）
                .path("/auth") // 限制接口发送
                .maxAge(jwtProperties.getRefreshTokenTTL())
                .sameSite("Strict")       // 防止 CSRF
                .build();
        // 8.refreshToken存入HttpOnly并返回
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return vo;
    }

    @Override
    public LoginVO refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        // 1. 从 Cookie 中获取 refreshToken
        String refreshToken = extractRefreshTokenFromCookie(request);
        // 1.1 校验参数
        if (StrUtil.isBlank(refreshToken)) {
            throw new BadRequestException("刷新令牌不能为空");
        }
        // 2. 判断令牌是否过期
        Long userId = jwtTool.parseToken(refreshToken);
        // 3. 基于token获取redis中的用户
        String key  = LOGIN_USER_KEY + refreshToken;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        // 4.判断用户是否存在
        if (userMap.isEmpty()) {
            throw new BadRequestException("刷新令牌无效");
        }
        // 5. 令牌轮换：生成全新的令牌对
        Duration accessTokenTTL = jwtProperties.getAccessTokenTTL();
        Duration refreshTokenTTL = jwtProperties.getRefreshTokenTTL();
        String newAccessToken = jwtTool.createToken(userId, accessTokenTTL);
        String newRefreshToken = jwtTool.createToken(userId, refreshTokenTTL);
        // 5.2拼接新的refreshKey
        String newKey  = LOGIN_USER_KEY + newRefreshToken;
        // 5.3删除redis用户
        stringRedisTemplate.delete(key);
        // 5.4重新添加redis用户
        stringRedisTemplate.opsForHash().putAll(newKey, userMap);
        stringRedisTemplate.expire(newKey, refreshTokenTTL);
        // 6. 封装并返回新令牌
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setAccessToken(newAccessToken);
        // 8.创建 HttpOnly
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)           // 防止 XSS 攻击
                .secure(true)             // 生产环境必须 true（HTTPS）
                .path("/auth") // 限制接口发送
                .maxAge(jwtProperties.getRefreshTokenTTL())
                .sameSite("Strict")       // 防止 CSRF
                .build();
        // 9.refreshToken存入HttpOnly并返回
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return vo;
    }

    @Override
    public Void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 从 Cookie 获取 refreshToken
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (StrUtil.isNotBlank(refreshToken)) {
            // 2. 删除 Redis 中的用户信息
            String key = LOGIN_USER_KEY + refreshToken;
            stringRedisTemplate.delete(key);
        }
        // 3. 清除客户端 Cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/auth/refresh")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
        return null;
    }

    /**
     * 从 Cookie 中提取 refreshToken
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}