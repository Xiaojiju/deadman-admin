package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信支付回调解析结果（渠道内部使用）。
 *
 * @param outTradeNo     平台支付单号
 * @param transactionId  微信交易单号
 * @param tradeState     微信交易状态
 * @param amountTotal    订单金额（分），渠道未返回时可为 null
 */
public record WechatPayNotifyParseResult(
        String outTradeNo, String transactionId, String tradeState, Integer amountTotal) {
}
