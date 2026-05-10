package com.pajamas.promotion.service;

import com.pajamas.promotion.domain.po.Coupon;
import com.pajamas.promotion.domain.po.ExchangeCode;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;

/**
 * <p>
 * 兑换码 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-06
 */
public interface IExchangeCodeService extends IService<ExchangeCode> {

    @Async("generateExchangeCodeExecutor")
    void asyncGenerateCode(Coupon coupon);

    boolean updateExchangeMark(long serialNum, boolean mark);

    Long exchangeTargetId(long serialNum);
}
