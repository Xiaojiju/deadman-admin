package com.mtfm.deadman.plugin.pay.spi.refund;

/**
 * 渠道退款回调解析结果。
 *
 * @param outRefundNo          平台退款单号
 * @param outTradeNo           平台支付单号
 * @param channelRefundId      渠道退款单号
 * @param channelTransactionId 渠道支付单号
 * @param amountRefund         本次退款金额（分），回调未提供时可为 null
 * @param targetStatus         退款状态
 * @param userReceivedAccount  退款入账账户描述（可选）
 * @param rawPayload           回调原文
 */
public record RefundNotifyResult(
        String outRefundNo,
        String outTradeNo,
        String channelRefundId,
        String channelTransactionId,
        Integer amountRefund,
        String targetStatus,
        String userReceivedAccount,
        String rawPayload) {
}
