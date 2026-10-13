package com.pajamas.pay.third.wx;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pajamas.common.utils.JsonUtils;
import com.pajamas.common.utils.StringUtils;
import com.pajamas.pay.sdk.constants.PayConstants;
import com.pajamas.pay.third.IPayService;
import com.pajamas.pay.third.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service(PayConstants.WX_CHANNEL_CODE)
@RequiredArgsConstructor
public class WxPayService implements IPayService {

    private final WxPayClient wxPayClient;

    @Override
    public PrepayResponse createPrepayOrder(String title, String orderNo, Integer amount) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
        // 2.请求参数
        ObjectNode baseParam = wxPayClient.baseParam(true, true, false)
                .put("out_trade_no", orderNo)
                .put("description", title);
        baseParam.putObject("amount").put("total", amount);

        // 3.发送请求
        String responseJson = wxPayClient.doPostJson(requestPath, baseParam);

        // 4.解析
        JSONObject result = JsonUtils.parseObj(responseJson);

        String codeUrl = result.getStr("code_url");
        if (codeUrl != null) {
            // 4.1.响应正常，返回支付链接
            return PrepayResponse.builder()
                    .success(true)
                    .payUrl(codeUrl)
                    .build();
        } else {
            // 4.2.响应异常，返回异常信息
            return PrepayResponse.builder()
                    .success(false)
                    .code(result.getStr("code"))
                    .msg(result.getStr("message"))
                    .detail(result.getStr("detail"))
                    .build();
        }
    }
    @Override
    public PrepayResponse createWxMiniPrepayOrder(String title, String orderNo, Integer amount, String openid) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
        // 2.请求参数
        ObjectNode baseParam = wxPayClient.baseParam(true, true, false)
                .put("out_trade_no", orderNo)
                .put("description", title);
        baseParam.putObject("amount").put("total", amount);
        baseParam.putObject("payer").put("openid", openid);
        // 3.发送请求
        String responseJson = wxPayClient.doPostJson(requestPath, baseParam);

        // 4.解析
        JSONObject result = JsonUtils.parseObj(responseJson);

        String prepayId = result.getStr("prepay_id");
        if (prepayId != null) {
            // 4.1.响应正常，返回支付链接
            return PrepayResponse.builder()
                    .success(true)
                    .prepayId(prepayId)
                    .build();
        } else {
            // 4.2.响应异常，返回异常信息
            return PrepayResponse.builder()
                    .success(false)
                    .code(result.getStr("code"))
                    .msg(result.getStr("message"))
                    .detail(result.getStr("detail"))
                    .build();
        }
    }
    @Override
    public PayLoadResponse code2Session(String code) {
        // 1.请求地址
        String requestPath = "https://api.weixin.qq.com/sns/jscode2session";
        // 2.请求参数（appid、secret、js_code、grant_type）
        String responseJson = wxPayClient.doGetJson(requestPath, false,
                new BasicNameValuePair("appid", "wx0ca99a203b1e9943"),
                new BasicNameValuePair("secret", ""),
                new BasicNameValuePair("js_code", code),
                new BasicNameValuePair("grant_type", "authorization_code")
        );
        // 3.解析
        JSONObject result = JsonUtils.parseObj(responseJson);
        String openid = result.getStr("openid");
        if (openid == null) {
            // 3.1.失败（HTTP 200 但带 errcode）
            return PayLoadResponse.builder()
                    .success(false)
                    .code(result.getInt("errcode"))
                    .msg(result.getStr("errmsg"))
                    .build();
        }
        // 3.2.成功
        return PayLoadResponse.builder()
                .success(true)
                .openId(openid)
                .sessionKey(result.getStr("session_key"))
                .unionId(result.getStr("unionid"))
                .build();
    }

    @Override
    public PayStatusResponse queryPayOrderStatus(String payOrderNo) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/" + payOrderNo;
        // 2.发送请求
        String responseJson = wxPayClient.doGetJson(requestPath, true);
        System.out.println("responseJson = " + responseJson);
        // 3.解析响应
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");
        LocalDateTime successTime = result.getLocalDateTime("success_time", LocalDateTime.now());
        // 3.1.请求异常
        if(StringUtils.isNotBlank(code)){
            return PayStatusResponse.builder().success(false).code(code).msg(message).build();
        }
        // 3.2.请求成功
        return PayStatusResponse.builder()
                .success(true)
                .payOrderNo(payOrderNo)
                .payStatus(parsePayStatus(result.getStr("trade_state")))
                .msg(result.getStr("trade_state_desc"))
                .totalAmount(result.getJSONObject("amount").getInt("total"))
                .successTime(successTime)
                .build()
                ;
    }

    @Override
    public PayCloseResponse closeWxOrder(String payOrderNo) {
        // 1. 请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/"
                + payOrderNo + "/close";

        // 2. 准备请求参数
        ObjectNode body = wxPayClient.baseParam(false, true, false);

        // 3. 发送请求
        String responseJson = wxPayClient.doPostJson(requestPath, body);

        // 4.1请求成功
        if(StringUtils.isBlank(responseJson)){
            return PayCloseResponse.builder().success(true).build();
        }

        // 4.1.请求异常,解析
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");

        return PayCloseResponse.builder().success(false).code(code).msg(message).build();
    }
    @Override
    public RefundResponse refundOrder(String payOrderNo, String refundOrderNo, Integer refundAmount, Integer totalAmount) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";
        // 2.准备请求参数
        ObjectNode baseParam = wxPayClient.baseParam(false, false, true)
                .put("out_trade_no", payOrderNo)
                .put("out_refund_no", refundOrderNo);
        baseParam.putObject("amount")
                .put("refund", refundAmount)
                .put("total", totalAmount)
                .put("currency", "CNY")
        ;
        // 3.发送请求
        String responseJson = wxPayClient.doPostJson(requestPath, baseParam);
        // 4.解析
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");
        // 4.1.请求异常
        if(StringUtils.isNotBlank(code)){
            return RefundResponse.builder().success(false).code(code).msg(message).build();
        }
        // 4.2.请求成功
        JSONObject amount = result.getJSONObject("amount");
        String status = result.getStr("status");
        return RefundResponse.builder()
                .success(true)
                .channel(result.getStr("channel"))
                .status(parseRefundStatus(status))
                .amount(amount == null ? 0 : amount.getInt("refund"))
                .build();
    }

    @Override
    public RefundResponse queryRefundStatus(String orderNo, String refundOrderNo) {
        // 1.请求地址
        String requestPath = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/" + refundOrderNo;
        // 2.发送请求
        String responseJson = wxPayClient.doGetJson(requestPath, false);
        // 3.解析响应
        JSONObject result = JsonUtils.parseObj(responseJson);
        String code = result.getStr("code");
        String message = result.getStr("message");
        // 3.1.请求异常
        if(StringUtils.isNotBlank(code)){
            return RefundResponse.builder().success(false).code(code).msg(message).build();
        }
        // 3.2.请求成功
        String status = result.getStr("status");
        JSONObject amount = result.getJSONObject("amount");
        return RefundResponse.builder()
                .success(true)
                .channel(result.getStr("channel"))
                .status(parseRefundStatus(status))
                .amount(amount == null ? 0 : amount.getInt("refund"))
                .build();
    }

    private Integer parseRefundStatus(String status) {
        switch (status) {
            case "PROCESSING":
                return 1;
            case "SUCCESS":
                return 2;
            default:
                return 3;
        }
    }

    private Integer parsePayStatus(String tradeState) {
        switch (tradeState) {
            case "NOTPAY":
                return 1;
            case "CLOSED":
                return 2;
            case "SUCCESS":
                return 3;
            case "REFUND":
                return 4;
            default:
                return 1;
        }
    }
}
