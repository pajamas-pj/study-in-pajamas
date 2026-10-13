package com.pajamas.user.service;

import com.pajamas.user.domain.dto.WxLoginDTO;
import com.pajamas.user.domain.po.UserThirdAccount;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户第三方账号关联 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-10-09
 */
public interface IUserThirdAccountService extends IService<UserThirdAccount> {

    void saveWxOpenId(WxLoginDTO wxLoginDTO);

    String queryWxMiniOpenIdByUserId(Long userId);
}
