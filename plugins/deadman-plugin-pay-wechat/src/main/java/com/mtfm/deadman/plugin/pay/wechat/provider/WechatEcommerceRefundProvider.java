package com.mtfm.deadman.plugin.pay.wechat.provider;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentChannelParams;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.refund.AbnormalRefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.EcommerceRefundProvider;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundQueryResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundParseResult;

import lombok.RequiredArgsConstructor;

/**
 * 微信收付通合单退款 Provider，{@code providerId} 与合单支付 Provider 一致。
 * <p>
 * 创建/查单/回调均走电商退款 API 与资源模型；须在 channelParams 或退款单上携带 {@code subMchid}。
 */
@Component
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-ecommerce-combine-jsapi",
        name = "enabled",
        havingValue = "true")
@RequiredArgsConstructor
public class WechatEcommerceRefundProvider implements EcommerceRefundProvider {

    /** 与合单支付 Provider 共用标识 */
    public static final String PROVIDER_ID = WechatPayProviderIds.WECHAT_ECOMMERCE_COMBINE_JSAPI;

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
        String subMchid = context.channelParam(PaymentChannelParams.SUB_MCHID);
        if (!StringUtils.hasText(subMchid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收付通退款缺少二级商户号 subMchid");
        }
        WechatRefundParseResult parsed = wechatPayApiGateway.createEcommerceRefund(new WechatEcommerceRefundCommand(
                subMchid.trim(),
                context.getOutTradeNo(),
                context.getChannelTransactionId(),
                outRefundNo,
                context.getReason(),
                context.getAmountRefund(),
                context.getAmountTotal(),
                binding.getRefundNotifyUrl()));
        return toRefundResult(parsed, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundNotifyResult parseRefundNotify(ChannelNotifyContext context) {
        WechatRefundParseResult parsed = wechatPayApiGateway.parseEcommerceRefundNotify(context);
        return toNotifyResult(parsed, context.rawBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundQueryResult queryRefund(String outRefundNo) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "收付通查退款必须提供 subMchid，请使用带 channelParams 的重载");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundQueryResult queryRefund(String outRefundNo, Map<String, String> channelParams) {
        String subMchid = channelParams == null ? null : channelParams.get(PaymentChannelParams.SUB_MCHID);
        if (!StringUtils.hasText(subMchid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收付通查退款缺少二级商户号 subMchid");
        }
        WechatRefundParseResult parsed =
                wechatPayApiGateway.queryEcommerceRefundByOutRefundNo(outRefundNo, subMchid.trim());
        return toQueryResult(parsed);
    }

    /**
     * 收付通电商退款暂不支持异常退款到银行卡。
     *
     * @param context 异常退款上下文
     * @return 不会返回
     */
    @Override
    public RefundResult createAbnormalRefund(AbnormalRefundContext context) {
        throw new BusinessException(ResultCode.PAY_ABNORMAL_REFUND_NOT_ALLOWED, "收付通电商退款不支持异常退款到银行卡");
    }

    /**
     * Mock 网关下自动查退款完成。
     */
    @Override
    public boolean autoCompleteAfterRefund() {
        return wechatPayPluginProperties.shouldUseMock();
    }

    private WechatPayProviderBindingProperties requireBinding() {
        WechatPayProviderBindingProperties binding = wechatPayPluginProperties.providerBinding(PROVIDER_ID);
        if (!binding.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信收付通合单 Provider 未启用");
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
