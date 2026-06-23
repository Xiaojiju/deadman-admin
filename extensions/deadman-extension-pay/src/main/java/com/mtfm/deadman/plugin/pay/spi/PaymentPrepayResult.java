package com.mtfm.deadman.plugin.pay.spi;

/**
 * 支付预下单结果。
 *
 * @param outTradeNo          平台支付单号
 * @param providerId          支付 Provider 标识
 * @param prepayId            渠道预支付 ID（如微信 prepay_id）
 * @param clientInvokeParams  客户端调起支付所需参数
 * @param channelExtra        渠道扩展信息，由上层写入支付单
 */
public record PaymentPrepayResult(
        String outTradeNo,
        String providerId,
        String prepayId,
        PaymentClientInvokeParams clientInvokeParams,
        PaymentChannelExtra channelExtra) {

    /**
     * 构造无客户端调起参数与渠道扩展信息的预下单结果。
     *
     * @param outTradeNo 平台支付单号
     * @param providerId 支付 Provider 标识
     * @param prepayId   渠道预支付 ID
     */
    public PaymentPrepayResult(String outTradeNo, String providerId, String prepayId) {
        this(outTradeNo, providerId, prepayId, null, null);
    }

    /**
     * 构造无渠道扩展信息的预下单结果。
     *
     * @param outTradeNo         平台支付单号
     * @param providerId         支付 Provider 标识
     * @param prepayId           渠道预支付 ID
     * @param clientInvokeParams 客户端调起支付参数
     */
    public PaymentPrepayResult(
            String outTradeNo, String providerId, String prepayId, PaymentClientInvokeParams clientInvokeParams) {
        this(outTradeNo, providerId, prepayId, clientInvokeParams, null);
    }
}
