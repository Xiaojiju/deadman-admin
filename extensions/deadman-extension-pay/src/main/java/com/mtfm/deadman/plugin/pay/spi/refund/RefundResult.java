package com.mtfm.deadman.plugin.pay.spi.refund;

/**
 * 渠道退款申请结果。
 *
 * @param outRefundNo          平台退款单号
 * @param outTradeNo           平台支付单号
 * @param channelRefundId      渠道退款单号
 * @param channelTransactionId 渠道支付单号
 * @param amountRefund         本次退款金额（分），渠道未返回时可为 null
 * @param targetStatus         退款状态（PROCESSING/SUCCESS/CLOSED/ABNORMAL）
 * @param userReceivedAccount  退款入账账户描述（可选）
 * @param rawPayload           渠道原文（可选）
 */
public record RefundResult(
        String outRefundNo,
        String outTradeNo,
        String channelRefundId,
        String channelTransactionId,
        Integer amountRefund,
        String targetStatus,
        String userReceivedAccount,
        String rawPayload) {
}
