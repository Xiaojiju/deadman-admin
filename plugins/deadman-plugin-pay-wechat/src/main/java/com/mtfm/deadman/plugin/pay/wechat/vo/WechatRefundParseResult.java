package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信退款解析/查单结果。
 *
 * @param outRefundNo          平台退款单号
 * @param outTradeNo           平台支付单号
 * @param channelRefundId      微信退款单号
 * @param channelTransactionId 微信支付单号
 * @param amountRefund         退款金额（分）
 * @param refundStatus         退款状态
 * @param userReceivedAccount  退款入账账户
 */
public record WechatRefundParseResult(
        String outRefundNo,
        String outTradeNo,
        String channelRefundId,
        String channelTransactionId,
        Integer amountRefund,
        String refundStatus,
        String userReceivedAccount) {
}
