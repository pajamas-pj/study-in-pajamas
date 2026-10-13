package com.pajamas.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.utils.UserContext;
import com.pajamas.pay.sdk.client.PayClient;
import com.pajamas.user.domain.dto.WxLoginDTO;
import com.pajamas.user.domain.po.UserThirdAccount;
import com.pajamas.user.mapper.UserThirdAccountMapper;
import com.pajamas.user.service.IUserThirdAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户第三方账号关联 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-10-09
 */
@Service
@RequiredArgsConstructor
public class UserThirdAccountServiceImpl extends ServiceImpl<UserThirdAccountMapper, UserThirdAccount> implements IUserThirdAccountService {

    private final PayClient payClient;

    @Override
    public void saveWxOpenId(WxLoginDTO wxLoginDTO) {
        //1.解析code获得openId
        String code = wxLoginDTO.getCode();
        String openid = payClient.queryWxMiniOpenIdByCode(code);
        //2.保存到数据库
        save(new UserThirdAccount()
                .setUserId(UserContext.getUser())
                .setPlatform(1)
                .setOpenId(openid));
    }

    @Override
    public String queryWxMiniOpenIdByUserId(Long userId) {
        UserThirdAccount account = lambdaQuery()
                .select(UserThirdAccount::getOpenId)
                .eq(UserThirdAccount::getUserId, userId)
                .eq(UserThirdAccount::getPlatform,1)
                .one();
        if(account == null){
            throw new BadRequestException("用户未绑定");
        }
        return account.getOpenId();
    }
}
