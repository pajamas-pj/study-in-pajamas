package com.pajamas.trade.service;


import com.pajamas.pay.sdk.dto.WxMiniPrepayDTO;
import com.pajamas.trade.domain.dto.OrderDelayQueryDTO;
import com.pajamas.trade.domain.dto.PayApplyFormDTO;
import com.pajamas.trade.domain.vo.PayChannelVO;

import java.util.List;

public interface IPayService {
    List<PayChannelVO> queryPayChannels();

    WxMiniPrepayDTO applyPayOrder(PayApplyFormDTO payApply);

    void queryPayResult(OrderDelayQueryDTO message);
}
