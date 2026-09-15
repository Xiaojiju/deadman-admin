package com.mtfm.deadman.plugin.pay.spi.payment;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;

/**
 * 支付 Provider SPI，各支付渠道插件实现此接口并注册为 Spring Bean。
 * 下层仅负责渠道 API 调用与回调解析，订单持久化由上层 PayService 统一处理。
 * <p>
 * 退款能力见 {@link com.mtfm.deadman.plugin.pay.spi.refund.RefundProvider}，勿在本接口混入退款方法。
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
     * 基于已有渠道预支付 ID 重新生成客户端调起参数（继续支付）。
     * <p>
     * 微信 JSAPI 的 timeStamp / paySign 会过期，但同一 {@code out_trade_no} 不能重复预下单，
     * 因此待支付订单应复用 {@code prepay_id} 重新签名，而不是再创建支付单。
     *
     * @param channelPrepayId 渠道预支付 ID（如微信 prepay_id）
     * @return 客户端调起支付参数
     */
    default PaymentClientInvokeParams rebuildClientInvokeParams(String channelPrepayId) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "当前支付方式不支持继续支付");
    }

    /**
     * 解析渠道支付回调，返回标准化结果供上层更新支付单状态。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    PaymentNotifyResult parseNotify(ChannelNotifyContext context);

    /**
     * 向支付渠道主动查询订单状态，用于回调延迟或丢失时的补偿同步。
     *
     * @param outTradeNo 平台支付单号
     * @return 查单结果
     */
    PaymentQueryResult queryOrder(String outTradeNo);

    /**
     * 预下单成功后是否由 PayService 自动完成支付（查单 → 更新状态 → 发布事件）。
     * <p>
     * Mock 等无真实回调的 Provider 可返回 true；真实渠道默认 false。
     *
     * @return 需要自动完成时返回 true
     */
    default boolean autoCompleteAfterPrepay() {
        return false;
    }
}
