package com.mtfm.deadman.plugin.pay.spi;

/**
 * 支付渠道查单结果，由 PaymentProvider 标准化后返回给上层。
 *
 * @param outTradeNo            平台支付单号
 * @param channelTransactionId  渠道支付单号
 * @param targetStatus          目标支付状态
 * @param rawPayload            渠道查单原始响应（可选）
 */
public record PaymentQueryResult(
        String outTradeNo, String channelTransactionId, String targetStatus, String rawPayload) {
}
