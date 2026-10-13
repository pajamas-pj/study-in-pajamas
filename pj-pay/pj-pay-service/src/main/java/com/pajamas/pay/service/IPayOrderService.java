package com.pajamas.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.pay.domain.po.PayOrder;
import com.pajamas.pay.sdk.dto.PayTimeoutDTO;
import com.pajamas.pay.sdk.dto.WxMiniPrepayDTO;
import com.pajamas.pay.sdk.dto.PayApplyDTO;
import com.pajamas.pay.sdk.dto.PayResultDTO;


import java.time.LocalDateTime;

/**
 * <p>
 * 支付订单 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-26
 */
public interface IPayOrderService extends IService<PayOrder> {

    String applyPayOrder(PayApplyDTO payApplyDTO);

    PayOrder queryByBizOrderNo(Long bizOrderNo);

    PayResultDTO queryPayResult(Long bizOrderId);

    PayOrder queryByPayOrderNo(Long payOrderNo);

    boolean markPayOrderSuccess(Long id, LocalDateTime successTime);

    PageDTO<PayOrder> queryPayingOrderByPage(int page, int size);

    void checkPayOrder(PayOrder payOrder);

    String queryWxMiniOpenIdByCode(String code);

    WxMiniPrepayDTO applyWxMiniPayOrder(PayApplyDTO payApplyDTO);

    void closePayOrder(PayTimeoutDTO payTimeoutDTO);
}
