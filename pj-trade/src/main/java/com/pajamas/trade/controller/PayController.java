package com.pajamas.trade.controller;


import com.pajamas.pay.sdk.dto.WxMiniPrepayDTO;
import com.pajamas.trade.domain.dto.PayApplyFormDTO;
import com.pajamas.trade.domain.vo.PayChannelVO;
import com.pajamas.trade.service.IPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "支付相关接口")
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final IPayService payService;

    @PostMapping("/order")
    @ApiOperation(value = "支付申请,返回package")
    public WxMiniPrepayDTO applyPayOrder(@RequestBody PayApplyFormDTO payApply) {
        return payService.applyPayOrder(payApply);
    }

    @GetMapping("/channels")
    @ApiOperation("获取支付渠道列表接口")
    public List<PayChannelVO> queryPayChannels() {
        return payService.queryPayChannels();
    }

}