package com.mtfm.deadman.plugin.pay.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundOrderStatusChangedPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 退款渠道结果短事务落库：状态回写、成功累加、事件发布在同一事务内完成。
 * <p>
 * 同步 {@code @EventListener} 将加入本事务；{@code @TransactionalEventListener(AFTER_COMMIT)}
 * 在提交后再执行（如异常退款异步处理）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundChannelResultApplier {

    private final PaymentRefundOrderService paymentRefundOrderService;
    private final PaymentOrderService paymentOrderService;
    private final RefundOrderStatusChangedPublisher refundOrderStatusChangedPublisher;

    /**
     * 应用渠道退款结果并在状态真实变更时发布事件。
     *
     * @param outRefundNo          平台退款单号
     * @param outTradeNo           平台支付单号（可空则取退款单；有值时必须与本地一致）
     * @param channelRefundId      渠道退款单号
     * @param channelTransactionId 渠道支付单号
     * @param targetStatus         目标状态
     * @param userReceivedAccount  入账账户
     * @param channelAmountRefund  渠道退款金额（分，可选；有值时必须与本地一致）
     * @param rawPayload           原文
     * @return 最新退款单快照
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundOrderSnapshot apply(
            String outRefundNo,
            String outTradeNo,
            String channelRefundId,
            String channelTransactionId,
            String targetStatus,
            String userReceivedAccount,
            Integer channelAmountRefund,
            String rawPayload) {
        PaymentRefundOrder before = paymentRefundOrderService.requireByOutRefundNo(outRefundNo);
        validateChannelConsistency(before, outTradeNo, channelAmountRefund);
        StatusTransitionResult transition = paymentRefundOrderService.applyChannelResult(
                outRefundNo,
                channelRefundId,
                channelTransactionId,
                targetStatus,
                userReceivedAccount,
                rawPayload);
        PaymentRefundOrder current = paymentRefundOrderService.reload(outRefundNo);
        if (transition.applied()
                && PaymentRefundStatus.SUCCESS.equals(transition.currentStatus())
                && !PaymentRefundStatus.SUCCESS.equals(transition.previousStatus())) {
            paymentOrderService.applySuccessfulRefund(
                    current.getOutTradeNo(),
                    current.getAmountRefund(),
                    firstNonBlank(channelTransactionId, current.getChannelTransactionId()));
        }
        if (transition.applied() && !transition.previousStatus().equals(transition.currentStatus())) {
            // 事务内发布：同步业务监听加入本事务；异常退款 AFTER_COMMIT 监听提交后再跑
            refundOrderStatusChangedPublisher.publish(
                    current, transition.previousStatus(), transition.currentStatus());
        }
        return toSnapshot(current);
    }

    private static void validateChannelConsistency(
            PaymentRefundOrder local, String channelOutTradeNo, Integer channelAmountRefund) {
        if (StringUtils.hasText(channelOutTradeNo)
                && StringUtils.hasText(local.getOutTradeNo())
                && !channelOutTradeNo.trim().equals(local.getOutTradeNo())) {
            log.error(
                    "退款回调支付单号与本地不一致：outRefundNo={}, localOutTradeNo={}, channelOutTradeNo={}",
                    local.getOutRefundNo(),
                    local.getOutTradeNo(),
                    channelOutTradeNo);
            throw new BusinessException(
                    ResultCode.PAY_REFUND_AMOUNT_MISMATCH,
                    "退款支付单号与本地不一致：" + local.getOutRefundNo());
        }
        if (channelAmountRefund != null
                && local.getAmountRefund() != null
                && !channelAmountRefund.equals(local.getAmountRefund())) {
            log.error(
                    "退款金额与本地不一致：outRefundNo={}, local={}, channel={}",
                    local.getOutRefundNo(),
                    local.getAmountRefund(),
                    channelAmountRefund);
            throw new BusinessException(
                    ResultCode.PAY_REFUND_AMOUNT_MISMATCH,
                    "退款金额与本地不一致：" + local.getOutRefundNo());
        }
    }

    private static RefundOrderSnapshot toSnapshot(PaymentRefundOrder order) {
        return new RefundOrderSnapshot(
                order.getOutRefundNo(),
                order.getOutTradeNo(),
                order.getBizOrderNo(),
                order.getProviderId(),
                order.getAmountRefund(),
                order.getAmountTotal(),
                order.getStatus(),
                order.getChannelRefundId(),
                order.getChannelTransactionId(),
                order.getReason(),
                order.getUserReceivedAccount(),
                order.getCreateTime(),
                order.getUpdateTime());
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return StringUtils.hasText(second) ? second.trim() : null;
    }
}
