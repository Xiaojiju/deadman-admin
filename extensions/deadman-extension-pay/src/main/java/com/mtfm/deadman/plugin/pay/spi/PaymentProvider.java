package com.mtfm.deadman.plugin.pay.spi;

/**
 * 支付 Provider SPI，各支付渠道插件实现此接口并注册为 Spring Bean。
 * 下层仅负责渠道 API 调用与回调解析，订单持久化由上层 PayService 统一处理。
 */
public interface PaymentProvider {

    /**
     * 提供商标识，全局唯一，如 {@code wechat-jsapi}。
     *
     * @return 提供商标识
     */
    String providerId();

    /**
     * 支付平台标识，如 {@code WECHAT}。
     *
     * @return 支付平台
     */
    String payPlatform();

    /**
     * 支付方式标识，如 {@code JSAPI}。
     *
     * @return 支付方式
     */
    String payMethod();

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
     * 创建渠道预下单，返回客户端调起支付所需参数。
     * 平台支付单号由上层生成并传入，Provider 不得自行持久化订单。
     *
     * @param context    预下单上下文
     * @param outTradeNo 平台支付单号
     * @return 预下单结果
     */
    PaymentPrepayResult createPrepay(PaymentPrepayContext context, String outTradeNo);

    /**
     * 解析渠道支付回调，返回标准化结果供上层更新支付单状态。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    PaymentNotifyResult parseNotify(PaymentNotifyContext context);

    /**
     * 向支付渠道主动查询订单状态，用于回调延迟或丢失时的补偿同步。
     *
     * @param outTradeNo 平台支付单号
     * @return 查单结果
     */
    PaymentQueryResult queryOrder(String outTradeNo);
}
