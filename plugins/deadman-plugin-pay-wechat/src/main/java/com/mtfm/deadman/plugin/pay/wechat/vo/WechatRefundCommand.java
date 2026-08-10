package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信退款申请命令。
 *
 * @param outTradeNo           平台支付单号
 * @param channelTransactionId 渠道支付单号（可选）
 * @param outRefundNo          平台退款单号
 * @param amountRefund         退款金额（分）
 * @param amountTotal          原订单金额（分）
 * @param currency             币种
 * @param reason               退款原因
 * @param notifyUrl            退款结果回调 URL
 */
public record WechatRefundCommand(
        String outTradeNo,
        String channelTransactionId,
        String outRefundNo,
        int amountRefund,
        int amountTotal,
        String currency,
        String reason,
        String notifyUrl) {
}
