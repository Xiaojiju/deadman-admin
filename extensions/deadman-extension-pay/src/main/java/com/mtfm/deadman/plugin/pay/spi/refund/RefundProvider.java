package com.mtfm.deadman.plugin.pay.spi.refund;

import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;

/**
 * 退款 Provider SPI，各支付渠道插件实现此接口并注册为 Spring Bean。
 * 下层仅负责渠道退款 API 与回调解析，退款单持久化由上层 RefundService 统一处理。
 */
public interface RefundProvider {

    /**
     * 提供商标识，须与对应 {@link com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider#providerId()} 一致。
     *
     * @return 提供商标识
     */
    String providerId();

    /**
     * 是否支持指定提供商标识。
     *
     * @param providerId 提供商标识
     * @return 是否支持
     */
    default boolean supports(String providerId) {
        return providerId().equals(providerId);
    }

    /**
     * 向渠道发起退款申请。
     * <p>
     * 平台退款单号由上层生成并传入；申请成功仅表示受理，终态以回调或查退款为准。
     *
     * @param context     退款上下文
     * @param outRefundNo 平台退款单号
     * @return 退款申请结果
     */
    RefundResult createRefund(RefundContext context, String outRefundNo);

    /**
     * 解析渠道退款结果回调。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    RefundNotifyResult parseRefundNotify(ChannelNotifyContext context);

    /**
     * 按平台退款单号向渠道查询退款状态。
     *
     * @param outRefundNo 平台退款单号
     * @return 查退款结果
     */
    RefundQueryResult queryRefund(String outRefundNo);

    /**
     * 发起异常退款（渠道退款状态为 ABNORMAL 时调用）。
     * <p>
     * 对应微信「发起异常退款」等接口；申请成功后状态通常回到 PROCESSING，终态仍以回调/查单为准。
     *
     * @param context 异常退款上下文
     * @return 异常退款申请结果
     */
    RefundResult createAbnormalRefund(AbnormalRefundContext context);

    /**
     * 发起退款后是否由 RefundService 自动按查退款结果完成（Mock 可用）。
     *
     * @return 需要自动完成时返回 true
     */
    default boolean autoCompleteAfterRefund() {
        return false;
    }
}
