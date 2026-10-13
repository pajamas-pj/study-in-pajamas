package com.pajamas.pay.service.impl;

import cn.hutool.crypto.PemUtil;
import com.alibaba.cola.statemachine.StateMachine;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.api.client.user.UserClient;
import com.pajamas.common.config.mq.RabbitMqHelper;
import com.pajamas.common.config.redisson.annotations.Lock;
import com.pajamas.common.config.redisson.enums.LockStrategy;
import com.pajamas.common.constants.MqConstants;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.exceptions.BizIllegalException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.StringUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.pay.constants.NotifyStatus;
import com.pajamas.pay.domain.enums.PayOrderEvent;
import com.pajamas.pay.domain.po.PayOrder;
import com.pajamas.pay.mapper.PayOrderMapper;
import com.pajamas.pay.sdk.constants.PayConstants;
import com.pajamas.pay.sdk.constants.PayErrorInfo;
import com.pajamas.pay.sdk.dto.*;
import com.pajamas.pay.service.IPayOrderService;
import com.pajamas.pay.third.CommonPayProperties;
import com.pajamas.pay.third.IPayService;
import com.pajamas.pay.third.model.*;
import com.pajamas.pay.third.wx.config.WxPayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static com.pajamas.pay.sdk.constants.PayErrorInfo.*;

/**
 * <p>
 * 支付订单 服务实现类
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    @Resource
    private Map<String, IPayService> payServiceChannels;
    private final RabbitMqHelper rabbitMqHelper;
    private final UserClient userClient;
    private final WxPayProperties properties;
    private final CommonPayProperties commonPayProperties;
    private final StateMachine<PayStatus, PayOrderEvent, PayOrderContext> payOrderStateMachine;
    @Lock(name = PayConstants.RedisKeyFormatter.PAY_APPLY, leaseTime = 3, autoUnlock = false)
    @Override
    public WxMiniPrepayDTO applyWxMiniPayOrder(PayApplyDTO payApplyDTO) {
        log.debug("准备创建小程序支付单，业务订单号：{}", payApplyDTO.getBizOrderNo());
        // 1.选择支付渠道
        IPayService payService = payServiceChannels.get(payApplyDTO.getPayChannelCode());
        if (payService == null) {
            log.error("用户选择的支付渠道不存在，业务订单号：{}", payApplyDTO.getBizOrderNo());
            throw new BadRequestException(INVALID_PAY_CHANNEL);
        }

        // 2.幂等性校验
        PayOrder payOrder = checkPayApplyIdempotent(payApplyDTO);
        if (StringUtils.isNotBlank(payOrder.getPrepayId())) {
            log.debug("预交易会话标识已经存在");
            return buildWxMiniPrepayDTO(payOrder.getPrepayId());
        }

        // 3.需要生成prepayId，调用第三方，获得prepayId
        String openid = queryWxMiniOpenIdByUserId(UserContext.getUser());
        PrepayResponse prepayResponse = payService.createWxMiniPrepayOrder(
                payApplyDTO.getOrderInfo(), payOrder.getPayOrderNo().toString(), payOrder.getAmount(),openid);

        // 4.更新预交易标识等信息到数据库
        updateWxMiniPayResult2DB(prepayResponse, payOrder.getId());
        if (!prepayResponse.isSuccess()) {
            log.error("生成prepayId失败，详情：{}", prepayResponse.getDetail());
            throw new BizIllegalException(PayErrorInfo.CREATE_PAY_ORDER_FAILED);
        }
        // 4. 发送延迟消息（30 分钟后检查是否支付）
        rabbitMqHelper.sendDelayMessage(
                MqConstants.Exchange.PAY_EXCHANGE,
                MqConstants.Key.ORDER_DELAY_KEY,
                PayTimeoutDTO.builder()
                        .payOrderId(payOrder.getId())
                        .payOrderNo(payOrder.getPayOrderNo())
                        .payOverTime(payOrder.getPayOverTime())
                        .payChannelCode(payOrder.getPayChannelCode())
                        .build(),
                Duration.ofMinutes(commonPayProperties.getPayOrderTTLMinutes())
        );
        // 5.组装小程序调起支付参数
        return buildWxMiniPrepayDTO(prepayResponse.getPrepayId());
    }
    @Override
    @Lock(name = PayConstants.RedisKeyFormatter.PAY_ORDER_CHECK_TASK, lockStrategy = LockStrategy.SKIP_AFTER_RETRY_TIMEOUT)
    public void checkPayOrder(PayOrder payOrder) {
        // 1.选择支付渠道
        IPayService payService = payServiceChannels.get(payOrder.getPayChannelCode());
        if (payService == null) {
            log.error("支付渠道不存在，业务单号：{}", payOrder.getBizOrderNo());
            // 异常订单，需要关闭支付单
            closeOrder(payOrder.getId());
            return;
        }
        // 2.判断订单是否超时
        if (payOrder.getPayOverTime().isBefore(LocalDateTime.now())) {
            log.debug("支付单{}已经超时，关闭订单", payOrder.getPayOrderNo());
            closeOrder(payOrder.getId());
            return;
        }
        // 3.查询支付状态
        PayStatusResponse response = payService.queryPayOrderStatus(payOrder.getPayOrderNo().toString());
        Integer payStatus = response.getPayStatus();
        // 3.1.判断是否查询失败或者待支付
        if (!response.isSuccess() || PayStatus.WAIT_BUYER_PAY.equalsValue(payStatus)) {
            // 查询异常或正在支付，结束
            return;
        }
        // 3.2.判断支付状态是否变更
        if (payStatus == payOrder.getStatus().getValue()) {
            // 支付状态没有变更
            return;
        }
        // 3.3.状态是支付成功或失败，直接更新订单状态
        updatePayStatus2DB(response, payOrder);
        // 3.4.判断状态是否支付成功，成功需要发送MQ消息通知
        if (PayStatus.TRADE_SUCCESS.equalsValue(response.getPayStatus())) {
            sendMqSuccessPayOrder(payOrder,response.getSuccessTime());
        }
    }
    @Lock(name = PayConstants.RedisKeyFormatter.PAY_ORDER_CLOSE_TASK, leaseTime = 3, autoUnlock = false)
    @Override
    public void closePayOrder(PayTimeoutDTO payTimeoutDTO) {
        log.debug("准备关闭小程序支付单，业务支付单号：{}", payTimeoutDTO.getPayOrderId());
        // 1.选择支付渠道
        IPayService payService = payServiceChannels.get(payTimeoutDTO.getPayChannelCode());
        if (payService == null) {
            log.error("用户选择的支付渠道不存在，业务支付单号：{}", payTimeoutDTO.getPayOrderId());
            throw new BadRequestException(INVALID_PAY_CHANNEL);
        }

        // 2.幂等性校验
        PayOrder payOrder = checkPayTimedOutIdempotent(payTimeoutDTO);
        if (payOrder == null) {
            log.info("幂等校验失败");
            return ;
        }

        // 3. 调微信关单
        PayCloseResponse payCloseResponse = payService.closeWxOrder(payOrder.getPayOrderNo().toString());

        // 4. 更新本地状态
        updatePayOrderClosed2DB(payCloseResponse,payOrder.getId());
        if(!payCloseResponse.isSuccess()){
            // 5. 关单失败
            String code = payCloseResponse.getCode();
            // 5.1 系统错误/频率超限 → 抛异常让 MQ 重试，不用查单
            if ("SYSTEM_ERROR".equals(code) || "FREQUENCY_LIMITED".equals(code)) {
                throw new BizIllegalException(CLOSE_PAY_ORDER_FAILED);
            }
            // 5.2 其他错误 → 调查单确认
            PayStatusResponse payStatusResponse = payService.queryPayOrderStatus(payTimeoutDTO.getPayOrderId().toString());
            Integer tradeState = payStatusResponse.getPayStatus();
            switch (tradeState) {
                case 1:
                    log.info("用户未支付，业务订单号：{}",payOrder.getPayOrderNo());
                    throw new BizIllegalException("关单失败，订单仍未支付，稍后重试");
                case 2:
                    //已关闭，更新状态
                    markPayOrderClosed(payOrder);
                    // 6. 通知 trade 关业务订单
                    sendMqClosePayOrder(payOrder);
                case 3:
                    log.info("用户已支付，无法关单，业务订单号：{}",payOrder.getPayOrderNo());
                    markPayOrderSuccess(payOrder);
                    // 6. 通知 pay 支付成功
                    sendMqSuccessPayOrder(payOrder,payStatusResponse.getSuccessTime());
                case 4:
                    // 异常：超时订单却处于退款状态 → 告警，人工介入
                    log.error("异常：超时订单处于退款状态");
                    return;
            }
        }
        // 6. 通知 trade 关业务订单
        sendMqClosePayOrder(payOrder);
    }
    private void sendMqClosePayOrder(PayOrder payOrder) {
        // 6. 通知 trade 关业务订单
        rabbitMqHelper.send(
                MqConstants.Exchange.ORDER_EXCHANGE,
                MqConstants.Key.ORDER_CLOSED,
                PayResultDTO.builder()
                        .payOrderNo(payOrder.getPayOrderNo())
                        .bizOrderId(payOrder.getBizOrderNo())
                        .build());
    }
    private void sendMqSuccessPayOrder(PayOrder payOrder, LocalDateTime successTime) {
        // 6. 通知 pay 支付成功
        rabbitMqHelper.send(
                MqConstants.Exchange.PAY_EXCHANGE,
                MqConstants.Key.PAY_SUCCESS,
                PayResultDTO.builder()
                        .payOrderNo(payOrder.getPayOrderNo())
                        .bizOrderId(payOrder.getBizOrderNo())
                        .payChannel(payOrder.getPayChannelCode())
                        .successTime(successTime)
                        .build());
    }
    private void markPayOrderSuccess(PayOrder payOrder) {
        PayStatus target = getPaySuccessState(payOrder);
        boolean updated = lambdaUpdate()
                .set(PayOrder::getStatus, target)
                .eq(PayOrder::getPayOrderNo, payOrder.getPayOrderNo())
                .eq(PayOrder::getStatus, payOrder.getStatus())
                .update();
        if (!updated) {
            log.warn("支付单状态更新失败，支付单号：{}", payOrder.getPayOrderNo());
            throw new BadRequestException(MARK_STATUS_ERROR);
        }
    }

    private void markPayOrderClosed(PayOrder payOrder) {
        PayStatus target = getPayCloseState(payOrder);
        boolean updated = lambdaUpdate()
                .set(PayOrder::getStatus, target)
                .eq(PayOrder::getId, payOrder.getId())
                .eq(PayOrder::getStatus, payOrder.getStatus())
                .update();
        if (!updated) {
            log.warn("支付单状态更新失败，支付单号：{}", payOrder.getPayOrderNo());
            throw new BadRequestException(MARK_STATUS_ERROR);
        }
    }

    private PayStatus getPaySuccessState(PayOrder payOrder) {
        PayOrderContext ctx = PayOrderContext.builder()
                .bizOrderNo(payOrder.getBizOrderNo())
                .payOrderNo(payOrder.getPayOrderNo())
                .operateTime(LocalDateTime.now()).build();
        PayStatus target = payOrderStateMachine.fireEvent(payOrder.getStatus(), PayOrderEvent.PAY_SUCCESS, ctx);
        if (target == null) {
            throw new BadRequestException(MARK_STATUS_ERROR);
        }
        return target;
    }

    private PayStatus getPayCloseState(PayOrder payOrder) {
        PayOrderContext ctx = PayOrderContext.builder()
                .bizOrderNo(payOrder.getBizOrderNo())
                .payOrderNo(payOrder.getPayOrderNo())
                .operateTime(LocalDateTime.now()).build();
        PayStatus target = payOrderStateMachine.fireEvent(payOrder.getStatus(), PayOrderEvent.CLOSE_ORDER, ctx);
        if (target == null) {
            throw new BadRequestException(MARK_STATUS_ERROR);
        }
        return target;
    }

    private void updatePayOrderClosed2DB(PayCloseResponse payCloseResponse, Long id) {
        try {
            lambdaUpdate()
                    .set(payCloseResponse.isSuccess(), PayOrder::getStatus, PayStatus.TRADE_CLOSED.getValue())
                    .set(!payCloseResponse.isSuccess(), PayOrder::getResultCode, payCloseResponse.getCode())
                    .set(!payCloseResponse.isSuccess(), PayOrder::getResultMsg, payCloseResponse.getMsg())
                    .eq(PayOrder::getId, id)
                    .update();
        } catch (Exception e) {
            log.error("更新支付单结果到数据时发生异常", e);
        }
    }

    private PayOrder checkPayTimedOutIdempotent(PayTimeoutDTO payTimeoutDTO) {
        // 1. 查支付单
        PayOrder payOrder = getById(payTimeoutDTO.getPayOrderId());
        if (payOrder == null) {
            log.error("支付单不存在，忽略，业务订房号={}", payTimeoutDTO.getPayOrderId());
            throw new BadRequestException(PAY_ORDER_NOT_FOUND);
        }
        // 2. 幂等：已支付/已关闭，忽略
        if (payOrder.success() || payOrder.closed()) {
            log.info("支付单已终态，忽略，业务订房号={}", payTimeoutDTO.getPayOrderId());
            return null;
        }
        // 3. 二次确认：是否真的超时（防止消息提前到达）
        if (payOrder.getPayOverTime().isAfter(LocalDateTime.now())) {
            log.warn("支付单未超时，业务订单号：{}", payTimeoutDTO.getPayOrderId());
            //靠定时任务兜底
            return null;
        }
        return payOrder;
    }

    private PayOrder checkPayApplyIdempotent(PayApplyDTO payApplyDTO) {
        // 1.首先插入支付单
        PayOrder payOrder = buildPayOrder(payApplyDTO);
        payOrder.setPayOrderNo(IdWorker.getId());
        boolean saved = save(payOrder);
        // 2.判断是否成功
        if (saved) {
            return payOrder;
        }
        // 3.旧单已经存在，查询支付
        PayOrder oldOrder = queryByBizOrderNo(payApplyDTO.getBizOrderNo());
        if (oldOrder == null) {
            throw new BizIllegalException("保存支付单失败且查不到旧单");
        }
        // 3.1旧单已经存在，判断是否支付成功
        if (PayStatus.TRADE_SUCCESS == oldOrder.getStatus()) {
            // 已经支付成功，抛出异常
            throw new BizIllegalException(PAY_ORDER_ALREADY_PAY_CODE, PAY_ORDER_ALREADY_PAY);
        }
        // 3.2旧单已经存在，判断是否已经关闭
        if (PayStatus.TRADE_CLOSED == oldOrder.getStatus()) {
            // 已经关闭，抛出异常
            throw new BizIllegalException(PAY_ORDER_ALREADY_CLOSE_CODE, PAY_ORDER_ALREADY_CLOSE);
        }
        // 5.旧单已经存在，判断支付渠道是否一致
        if (!StringUtils.equals(oldOrder.getPayChannelCode(), payApplyDTO.getPayChannelCode())) {
            // 支付渠道不一致，需要重置数据，然后重新申请支付单
            return reBuildPayOrder(payApplyDTO, oldOrder);
        }
        // 6.旧单已经存在,判断 prepayId 是否过期
        //    微信 prepay_id 有效期 2 小时
        //    保守起见，超过 1.5 小时就重新下单
        if (oldOrder.getCreateTime() != null
                && oldOrder.getCreateTime().plusMinutes(90).isBefore(LocalDateTime.now())) {
            // prepayId可能已过期，需要重置数据，然后重新申请支付单
            return reBuildPayOrder(payApplyDTO, oldOrder);
        }
        // 7.旧单已经存在，且可能是未支付或未提交，且支付渠道一致，直接返回旧数据
        return oldOrder;
    }

    private PayOrder reBuildPayOrder(PayApplyDTO payApplyDTO, PayOrder oldOrder) {
        PayOrder newOrder = buildPayOrder(payApplyDTO);
        newOrder.setId(oldOrder.getId());
        newOrder.setPrepayId("");
        updateById(newOrder);
        //微信的out_order_no不能复用
        newOrder.setPayOrderNo(IdWorker.getId());
        return newOrder;
    }

    private PayOrder buildPayOrder(PayApplyDTO payApplyDTO) {
        // 1.数据转换
        PayOrder payOrder = BeanUtils.toBean(payApplyDTO, PayOrder.class);
        // 2.初始化数据
        payOrder.setNotifyTimes(0);
        payOrder.setNotifyStatus(NotifyStatus.UN_CALL.getValue());
        payOrder.setPayOverTime(LocalDateTime.now().plusMinutes(120L));
        payOrder.setStatus(PayStatus.NOT_COMMIT);
        return payOrder;
    }

    private void updateWxMiniPayResult2DB(PrepayResponse prepayResponse, Long payOrderId) {
        try {
            lambdaUpdate()
                    .set(prepayResponse.isSuccess(), PayOrder::getPrepayId, prepayResponse.getPrepayId())
                    .set(prepayResponse.isSuccess(), PayOrder::getStatus, PayStatus.WAIT_BUYER_PAY.getValue())
                    .set(!prepayResponse.isSuccess(), PayOrder::getResultCode, prepayResponse.getCode())
                    .set(!prepayResponse.isSuccess(), PayOrder::getResultMsg, prepayResponse.getMsg())
                    .eq(PayOrder::getId, payOrderId)
                    .update();
        } catch (Exception e) {
            log.error("更新支付单结果到数据时发生异常", e);
        }
    }
    private void updatePayStatus2DB(PayStatusResponse response, PayOrder payOrder) {
        PayStatus target = getPaySuccessState(payOrder);
        try {
            lambdaUpdate()
                    .set(PayOrder::getStatus, target)
                    .set(PayOrder::getResultCode, response.getCode() == null ? "" : response.getCode())
                    .set(PayOrder::getResultMsg, response.getMsg() == null ? "" : response.getMsg())
                    .eq(PayOrder::getId, payOrder.getId())
                    .eq(PayOrder::getStatus,payOrder.getStatus())
                    .update();
        } catch (Exception e) {
            log.error("更新支付单结果到数据时发生异常", e);
        }
    }

    @Override
    @Lock(name = PayConstants.RedisKeyFormatter.PAY_APPLY, leaseTime = 3, autoUnlock = false)
    public String applyPayOrder(PayApplyDTO payApplyDTO) {
        log.debug("准备创建支付单，业务订单号：{}", payApplyDTO.getBizOrderNo());
        // 1.选择支付渠道
        IPayService payService = payServiceChannels.get(payApplyDTO.getPayChannelCode());
        if (payService == null) {
            log.error("用户选择的支付渠道不存在，业务单号：{}", payApplyDTO.getBizOrderNo());
            throw new BadRequestException(INVALID_PAY_CHANNEL);
        }

        // 2.幂等性校验
        PayOrder payOrder = checkIdempotent(payApplyDTO);
        if (StringUtils.isNotBlank(payOrder.getQrCodeUrl())) {
            log.debug("支付链接已经存在，不再重新创建，直接返回");
            return payOrder.getQrCodeUrl();
        }

        // 3.需要生成新支付链接，调用第三方，完成支付单下单
        PrepayResponse prepayResponse = payService.createPrepayOrder(
                payApplyDTO.getOrderInfo(), payOrder.getPayOrderNo().toString(), payOrder.getAmount());

        // 4.更新支付链接等信息到数据库
        updatePayResult2DB(prepayResponse, payOrder.getId());

        if (!prepayResponse.isSuccess()) {
            log.error("预创建支付单失败，详情：{}", prepayResponse.getDetail());
            throw new BizIllegalException(PayErrorInfo.CREATE_PAY_ORDER_FAILED);
        }
        // 5.返回支付链接
        log.debug("支付单生成成功，返回支付链接：{}", prepayResponse.getPayUrl());
        return prepayResponse.getPayUrl();
    }

    private void updatePayResult2DB(PrepayResponse prepayResponse, Long payOrderId) {
        try {
            lambdaUpdate()
                    .set(prepayResponse.isSuccess(), PayOrder::getPrepayId, prepayResponse.getPrepayId())
                    .set(prepayResponse.isSuccess(), PayOrder::getStatus, PayStatus.WAIT_BUYER_PAY.getValue())
                    .set(!prepayResponse.isSuccess(), PayOrder::getResultCode, prepayResponse.getCode())
                    .set(!prepayResponse.isSuccess(), PayOrder::getResultMsg, prepayResponse.getMsg())
                    .eq(PayOrder::getId, payOrderId)
                    .update();
        } catch (Exception e) {
            log.error("更新支付单结果到数据时发生异常", e);
        }
    }
    private PayOrder checkIdempotent(PayApplyDTO payApplyDTO) {
        // 1.首先查询支付订单
        PayOrder oldOrder = queryByBizOrderNo(payApplyDTO.getBizOrderNo());
        // 2.判断是否存在
        if (oldOrder == null) {
            // 不存在支付单，说明是第一次，写入新的支付单并返回
            PayOrder payOrder = buildPayOrder(payApplyDTO);
            payOrder.setPayOrderNo(IdWorker.getId());
            save(payOrder);
            return payOrder;
        }
        // 3.旧单已经存在，判断是否支付成功
        if (PayStatus.TRADE_SUCCESS == oldOrder.getStatus()) {
            // 已经支付成功，抛出异常
            throw new BizIllegalException(PAY_ORDER_ALREADY_PAY_CODE, PAY_ORDER_ALREADY_PAY);
        }
        // 4.旧单已经存在，判断是否已经关闭
        if (PayStatus.TRADE_CLOSED == oldOrder.getStatus()) {
            // 已经关闭，抛出异常
            throw new BizIllegalException(PAY_ORDER_ALREADY_CLOSE_CODE, PAY_ORDER_ALREADY_CLOSE);
        }
        // 5.旧单已经存在，判断支付渠道是否一致
        if (!StringUtils.equals(oldOrder.getPayChannelCode(), payApplyDTO.getPayChannelCode())) {
            // 支付渠道不一致，需要重置数据，然后重新申请支付单
            PayOrder payOrder = buildPayOrder(payApplyDTO);
            payOrder.setId(oldOrder.getId());
            payOrder.setQrCodeUrl("");
            updateById(payOrder);
            payOrder.setPayOrderNo(oldOrder.getPayOrderNo());
            return payOrder;
        }
        // 6.旧单已经存在，且可能是未支付或未提交，且支付渠道一致，直接返回旧数据
        return oldOrder;
    }

    @Override
    public PayOrder queryByBizOrderNo(Long bizOrderNo) {
        return lambdaQuery()
                .eq(PayOrder::getBizOrderNo, bizOrderNo)
                .one();
    }

    @Override
    public PayResultDTO queryPayResult(Long bizOrderNo) {
        // 1.查询支付单
        PayOrder payOrder = queryByBizOrderNo(bizOrderNo);
        if (payOrder == null) {
            throw new BizIllegalException(PAY_ORDER_NOT_FOUND);
        }
        // 2.判断支付状态
        if (payOrder.success()) {
            // 2.1.支付成功
            return PayResultDTO.builder()
                    .payOrderNo(payOrder.getPayOrderNo())
                    .successTime(payOrder.getPaySuccessTime())
                    .payChannel(payOrder.getPayChannelCode())
                    .build();
        }
        // 2.2.未支付
        if (payOrder.notCommit() || payOrder.waitBuyerPay()) {
            return PayResultDTO.builder()
                    .status(PayStatus.WAIT_BUYER_PAY.getValue())
                    .build();
        }
        // 2.2.支付失败
        return PayResultDTO.builder()
                .status(PayStatus.TRADE_CLOSED.getValue())
                .msg(payOrder.getResultMsg())
                .build();
    }

    @Override
    public PayOrder queryByPayOrderNo(Long payOrderNo) {
        return lambdaQuery().eq(PayOrder::getPayOrderNo, payOrderNo).one();
    }

    @Override
    public boolean markPayOrderSuccess(Long id, LocalDateTime successTime) {
        return lambdaUpdate()
                .set(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue())
                .set(PayOrder::getNotifyStatus, NotifyStatus.CALLING.getValue())
                .set(PayOrder::getPaySuccessTime, successTime)
                .eq(PayOrder::getId, id)
                // 支付状态的乐观锁判断
                .in(PayOrder::getStatus, PayStatus.NOT_COMMIT.getValue(), PayStatus.WAIT_BUYER_PAY.getValue())
                .update();
    }

    @Override
    public PageDTO<PayOrder> queryPayingOrderByPage(int pageNo, int size) {
        // 1.分页和排序条件
        Page<PayOrder> page = new Page<>(pageNo, size);
        page.addOrder(new OrderItem("id", true));
        // 2.查询
        Page<PayOrder> result = lambdaQuery()
                .eq(PayOrder::getStatus, PayStatus.WAIT_BUYER_PAY.getValue())
                .page(page);
        return PageDTO.of(result);
    }

    @Override
    public String queryWxMiniOpenIdByCode(String code) {
        IPayService payService = payServiceChannels.get(PayConstants.WX_CHANNEL_CODE);
        PayLoadResponse payLoadResponse = payService.code2Session(code);
        return payLoadResponse.getOpenId();
    }

    public String queryWxMiniOpenIdByUserId(Long userId) {
        String openid = userClient.queryWxMiniOpenIdByUserId(userId);
        if(openid == null){
            throw new BadRequestException("用户未绑定小程序");
        }
        return openid;
    }

    private void closeOrder(Long id) {
        PayOrder payOrder = new PayOrder();
        payOrder.setId(id);
        payOrder.setStatus(PayStatus.TRADE_CLOSED);
        updateById(payOrder);
    }


    /**
     * 组装小程序调起支付参数（含 paySign 签名）
     */
    private WxMiniPrepayDTO buildWxMiniPrepayDTO(String prepayId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String packageValue = "prepay_id=" + prepayId;
        // 4.对使用字段appId、timeStamp、nonceStr、package计算得出的签名值
        String paySign = generatePaySign(properties.getAppId(), timeStamp, nonceStr, packageValue);
        return WxMiniPrepayDTO.builder()
                .timeStamp(timeStamp)
                .nonceStr(nonceStr)
                .packageValue(packageValue)
                .signType("RSA")
                .paySign(paySign)
                .build();
    }
    /**
     * 生成 paySign
     * 待签名串：appId\ntimeStamp\nnonceStr\npackage\n
     * 用商户私钥做 SHA256withRSA，Base64 编码
     */
    private String generatePaySign(String appId, String timeStamp, String nonceStr, String packageValue) {
        try {
            String message = appId + "\n"
                    + timeStamp + "\n"
                    + nonceStr + "\n"
                    + packageValue + "\n";

            // 加载商户私钥
            PrivateKey privateKey = PemUtil.readPemPrivateKey(
                    new ByteArrayInputStream(properties.getPrivateKey().getBytes(StandardCharsets.UTF_8)));
            // SHA256withRSA 签名
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] signature = sign.sign();
            // Base64 编码
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            log.error("生成 paySign 失败", e);
            throw new BizIllegalException("生成微信支付签名失败");
        }
    }
}
