package com.mtfm.deadman.plugin.pay.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOrderStatusChangedPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付渠道结果短事务落库：状态回写与事件发布在同一事务内完成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentChannelResultApplier {

    private final PaymentOrderService paymentOrderService;
    private final PaymentOrderStatusChangedPublisher paymentOrderStatusChangedPublisher;

    /**
     * 应用渠道支付结果并在状态真实变更时发布事件。
     *
     * @param outTradeNo           平台支付单号
     * @param channelTransactionId 渠道支付单号
     * @param targetStatus         目标状态
     * @param channelAmountTotal   渠道订单金额（分，可选；有值时必须与本地一致）
     * @param rawPayload           渠道原文
     * @return 最新支付单快照
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrderSnapshot apply(
            String outTradeNo,
            String channelTransactionId,
            String targetStatus,
            Integer channelAmountTotal,
            String rawPayload) {
        PaymentOrder before = paymentOrderService.requireByOutTradeNo(outTradeNo);
        validateChannelAmount(before, channelAmountTotal, targetStatus);
        StatusTransitionResult transition = paymentOrderService.transitionStatus(
                outTradeNo, channelTransactionId, targetStatus, rawPayload);
        PaymentOrder current = paymentOrderService.reload(outTradeNo);
        if (transition.applied() && !transition.previousStatus().equals(transition.currentStatus())) {
            paymentOrderStatusChangedPublisher.publish(
                    current, transition.previousStatus(), transition.currentStatus());
        }
        return toSnapshot(current);
    }

    private static void validateChannelAmount(
            PaymentOrder local, Integer channelAmountTotal, String targetStatus) {
        if (channelAmountTotal == null) {
            if (PaymentOrderStatus.SUCCESS.equals(targetStatus)) {
                log.warn(
                        "支付成功结果缺少渠道金额，跳过金额校验：outTradeNo={}",
                        local.getOutTradeNo());
            }
            return;
        }
        if (local.getAmountTotal() != null && !channelAmountTotal.equals(local.getAmountTotal())) {
            log.error(
                    "支付金额与本地不一致：outTradeNo={}, local={}, channel={}",
                    local.getOutTradeNo(),
                    local.getAmountTotal(),
                    channelAmountTotal);
            throw new BusinessException(
                    ResultCode.PAY_AMOUNT_MISMATCH, "支付金额与本地不一致：" + local.getOutTradeNo());
        }
    }

    private static PaymentOrderSnapshot toSnapshot(PaymentOrder order) {
        return new PaymentOrderSnapshot(
                order.getOutTradeNo(),
                order.getBizOrderNo(),
                order.getProviderId(),
                order.getPayPlatform(),
                order.getPayMethod(),
                order.getDescription(),
                order.getAmountTotal(),
                order.getAmountRefunded() == null ? 0 : order.getAmountRefunded(),
                order.getStatus(),
                order.getChannelPrepayId(),
                order.getChannelTransactionId(),
                order.getCreateTime(),
                order.getUpdateTime());
    }
}
