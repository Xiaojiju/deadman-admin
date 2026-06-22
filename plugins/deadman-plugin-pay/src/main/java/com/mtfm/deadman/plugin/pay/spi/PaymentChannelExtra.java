package com.mtfm.deadman.plugin.pay.spi;

/**
 * 支付渠道扩展信息，持久化至 {@code payment_order.channel_extra}。
 *
 * @param openid 微信付款人 openid（JSAPI 场景）
 */
public record PaymentChannelExtra(String openid) {
}
