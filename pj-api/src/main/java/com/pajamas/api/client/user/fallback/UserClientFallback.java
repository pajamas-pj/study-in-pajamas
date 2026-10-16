package com.pajamas.api.client.user.fallback;


import com.pajamas.api.client.user.UserClient;
import com.pajamas.api.dto.user.LoginFormDTO;
import com.pajamas.api.dto.user.LoginUserDTO;
import com.pajamas.api.dto.user.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;
import java.util.List;

@Slf4j
public class UserClientFallback implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        log.error("查询用户服务出现异常", cause);
        return new UserClient() {
            @Override
            public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO) {
                return null;
            }
            @Override
            public Long exchangeUserIdWithPhone(String phone) {
                return null;
            }

            @Override
            public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff) {
                return null;
            }

            @Override
            public Integer queryUserType(Long id) {
                return null;
            }

            @Override
            public List<UserDTO> queryUserByIds(Iterable<Long> ids) {
                return Collections.emptyList();
            }

            @Override
            public UserDTO queryUserById(Long id) {
                return null;
            }

            @Override
            public String queryWxMiniOpenIdByUserId(Long userId) {
                return "";
            }
        };
    }
}
