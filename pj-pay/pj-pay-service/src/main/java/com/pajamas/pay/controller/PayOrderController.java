package com.pajamas.pay.controller;



import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.pay.sdk.dto.PayTimeoutDTO;
import com.pajamas.pay.sdk.dto.WxMiniPrepayDTO;
import com.pajamas.pay.sdk.constants.PayErrorInfo;
import com.pajamas.pay.sdk.constants.PayType;
import com.pajamas.pay.sdk.dto.PayApplyDTO;
import com.pajamas.pay.sdk.dto.PayResultDTO;
import com.pajamas.pay.service.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 支付订单 前端控制器
 * </p>
 */
@Api(tags = "支付相关接口")
@RestController
@RequestMapping("/pay-orders")
@RequiredArgsConstructor
public class PayOrderController {

    private final IPayOrderService payOrderService;

    @ApiOperation("扫码支付申请支付单，返回支付url地址，用于生产二维码")
    @PostMapping
    public String applyPayOrder(@RequestBody PayApplyDTO payApplyDTO){
        if(!PayType.NATIVE.equalsValue(payApplyDTO.getPayType())){
            throw new BadRequestException(PayErrorInfo.INVALID_PAY_TYPE);
        }
        return payOrderService.applyPayOrder(payApplyDTO);
    }

    @ApiOperation("根据业务端订单id查询支付结果")
    @GetMapping("/{bizOrderId}/status")
    public PayResultDTO queryPayResult(
            @ApiParam("业务订单id") @PathVariable("bizOrderId") Long bizOrderId
    ){
        return payOrderService.queryPayResult(bizOrderId);
    }

    @ApiOperation("根据wx.login的code查询用户openId")
    @GetMapping("/openId")
    public String queryOpenIdByCode(
            @ApiParam("微信接口返回的code") @RequestParam("code")String code
    ){
        return payOrderService.queryWxMiniOpenIdByCode(code);
    }

    @ApiOperation("小程序申请预支付，返回prepay_id预支付交易id，用于小程序前端调支付")
    @PostMapping("/wxMini/prepay")
    public WxMiniPrepayDTO applyWxMiniPayOrder(@RequestBody PayApplyDTO payApplyDTO){
        if(!PayType.MINI_APP.equalsValue(payApplyDTO.getPayType())){
            throw new BadRequestException(PayErrorInfo.INVALID_PAY_TYPE);
        }
        return payOrderService.applyWxMiniPayOrder(payApplyDTO);
    }

    @ApiOperation("关单")
    @PostMapping("/close")
    public void closePayOrder(@RequestBody PayTimeoutDTO payTimeoutDTO){
        if(!PayType.MINI_APP.equalsValue(payTimeoutDTO.getPayType())){
            throw new BadRequestException(PayErrorInfo.INVALID_PAY_TYPE);
        }
        payOrderService.closePayOrder(payTimeoutDTO);
    }
}
