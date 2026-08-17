package com.mtfm.deadman.plugin.pay.spi.refund;

/**
 * 收付通 / 电商退款 SPI 语义标记（文档与类型指引）。
 * <p>
 * 实现上仍注册为 {@link RefundProvider}，且 {@code providerId()} 必须与对应
 * {@link com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider#providerId()} 一致，
 * 以便 {@link com.mtfm.deadman.plugin.pay.service.RefundService} 按支付单路由。
 * <p>
 * 微信落地类：{@code WechatEcommerceRefundProvider}（合单支付同 ID）。
 * 业务请经 {@link com.mtfm.deadman.plugin.pay.facade.EcommerceTradeFacade#createRefund} 发起，
 * 并在 {@link RefundContext} 的 channelParams 中传入 {@code subMchid}。
 */
public interface EcommerceRefundProvider extends RefundProvider {
}
