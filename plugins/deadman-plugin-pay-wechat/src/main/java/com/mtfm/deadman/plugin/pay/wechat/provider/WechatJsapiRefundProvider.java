package com.mtfm.deadman.plugin.pay.wechat.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.refund.AbnormalRefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundProvider;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundQueryResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAbnormalRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundParseResult;

import lombok.RequiredArgsConstructor;

/**
 * 微信小程序 JSAPI 退款 Provider，仅负责微信退款 API 与回调解析。
 */
@Component
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-jsapi",
        name = "enabled",
        havingValue = "true")
@RequiredArgsConstructor
public class WechatJsapiRefundProvider implements RefundProvider {

    /** 与支付 Provider 共用标识 */
    public static final String PROVIDER_ID = WechatPayProviderIds.WECHAT_JSAPI;

    private final WechatPayApiGateway wechatPayApiGateway;
    private final WechatPayPluginProperties wechatPayPluginProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResult createRefund(RefundContext context, String outRefundNo) {
        WechatPayProviderBindingProperties binding = requireBinding();
        WechatRefundParseResult parsed = wechatPayApiGateway.createRefund(new WechatRefundCommand(
                context.getOutTradeNo(),
                context.getChannelTransactionId(),
                outRefundNo,
                context.getAmountRefund(),
                context.getAmountTotal(),
                context.getCurrency(),
                context.getReason(),
                binding.getRefundNotifyUrl()));
        return toRefundResult(parsed, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundNotifyResult parseRefundNotify(ChannelNotifyContext context) {
        WechatRefundParseResult parsed = wechatPayApiGateway.parseRefundNotify(context);
        return toNotifyResult(parsed, context.rawBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundQueryResult queryRefund(String outRefundNo) {
        WechatRefundParseResult parsed = wechatPayApiGateway.queryRefundByOutRefundNo(outRefundNo);
        return toQueryResult(parsed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResult createAbnormalRefund(AbnormalRefundContext context) {
        requireBinding();
        WechatRefundParseResult parsed = wechatPayApiGateway.createAbnormalRefund(new WechatAbnormalRefundCommand(
                context.getChannelRefundId(),
                context.getOutRefundNo(),
                context.getReceiveType(),
                context.getBankType(),
                context.getBankAccount(),
                context.getRealName()));
        return toRefundResult(parsed, null);
    }

    /**
     * Mock 网关下自动查退款完成，真实微信默认走回调。
     */
    @Override
    public boolean autoCompleteAfterRefund() {
        return wechatPayPluginProperties.shouldUseMock();
    }

    private WechatPayProviderBindingProperties requireBinding() {
        WechatPayProviderBindingProperties binding = wechatPayPluginProperties.providerBinding(PROVIDER_ID);
        if (!binding.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信 JSAPI Provider 未启用");
        }
        return binding;
    }

    private static RefundResult toRefundResult(WechatRefundParseResult parsed, String rawPayload) {
        return new RefundResult(
                parsed.outRefundNo(),
                parsed.outTradeNo(),
                parsed.channelRefundId(),
                parsed.channelTransactionId(),
                parsed.amountRefund(),
                normalizeStatus(parsed.refundStatus()),
                parsed.userReceivedAccount(),
                rawPayload);
    }

    private static RefundNotifyResult toNotifyResult(WechatRefundParseResult parsed, String rawPayload) {
        return new RefundNotifyResult(
                parsed.outRefundNo(),
                parsed.outTradeNo(),
                parsed.channelRefundId(),
                parsed.channelTransactionId(),
                parsed.amountRefund(),
                normalizeStatus(parsed.refundStatus()),
                parsed.userReceivedAccount(),
                rawPayload);
    }

    private static RefundQueryResult toQueryResult(WechatRefundParseResult parsed) {
        return new RefundQueryResult(
                parsed.outRefundNo(),
                parsed.outTradeNo(),
                parsed.channelRefundId(),
                parsed.channelTransactionId(),
                parsed.amountRefund(),
                normalizeStatus(parsed.refundStatus()),
                parsed.userReceivedAccount(),
                null);
    }

    private static String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return PaymentRefundStatus.PROCESSING;
        }
        return switch (status.trim().toUpperCase()) {
            case "SUCCESS" -> PaymentRefundStatus.SUCCESS;
            case "CLOSED" -> PaymentRefundStatus.CLOSED;
            case "ABNORMAL" -> PaymentRefundStatus.ABNORMAL;
            case "PROCESSING" -> PaymentRefundStatus.PROCESSING;
            default -> throw new BusinessException(ResultCode.PAY_REFUND_NOTIFY_PARSE_FAILED, "未知退款状态：" + status);
        };
    }
}
