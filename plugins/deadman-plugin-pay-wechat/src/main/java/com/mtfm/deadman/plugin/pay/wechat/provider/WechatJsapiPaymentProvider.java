package com.mtfm.deadman.plugin.pay.wechat.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentMethod;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.spi.PaymentChannelExtra;
import com.mtfm.deadman.plugin.pay.spi.PaymentClientInvokeParams;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.PaymentQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayJsapiPrepayResult;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayChannelParams;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayTradeStateMapper;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;

import lombok.RequiredArgsConstructor;

/**
 * 微信小程序 JSAPI 支付 Provider 实现，仅负责微信渠道 API 与回调解析。
 */
@Component
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-jsapi",
        name = "enabled",
        havingValue = "true")
@RequiredArgsConstructor
public class WechatJsapiPaymentProvider implements PaymentProvider {

    /** Provider 标识，与 deadman.plugin.pay.default-provider 对齐 */
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
    public String payPlatform() {
        return PaymentPlatform.WECHAT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String payMethod() {
        return PaymentMethod.JSAPI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context, String outTradeNo) {
        String openid = requireOpenid(context);
        WechatPayProviderBindingProperties binding = requireBinding();
        WechatPayJsapiPrepayResult prepayResult = wechatPayApiGateway.createJsapiPrepay(new WechatJsapiPrepayCommand(
                outTradeNo,
                context.getDescription(),
                context.getAmountTotal(),
                openid,
                requireAppId(binding),
                binding.getNotifyUrl()));
        return new PaymentPrepayResult(
                outTradeNo,
                PROVIDER_ID,
                prepayResult.prepayId(),
                toClientInvokeParams(prepayResult.requestPayment()),
                new PaymentChannelExtra(openid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentNotifyResult parseNotify(PaymentNotifyContext context) {
        WechatPayNotifyParseResult parsed = wechatPayApiGateway.parseNotify(context);
        return toNotifyResult(parsed, context.rawBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentQueryResult queryOrder(String outTradeNo) {
        WechatPayNotifyParseResult parsed = wechatPayApiGateway.queryOrderByOutTradeNo(outTradeNo);
        return toQueryResult(parsed);
    }

    private WechatPayProviderBindingProperties requireBinding() {
        WechatPayProviderBindingProperties binding = wechatPayPluginProperties.providerBinding(PROVIDER_ID);
        if (!binding.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信 JSAPI Provider 未启用");
        }
        return binding;
    }

    private static String requireAppId(WechatPayProviderBindingProperties binding) {
        if (!StringUtils.hasText(binding.getAppId())) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信 JSAPI AppId 未配置");
        }
        return binding.getAppId().trim();
    }

    private static String requireOpenid(PaymentPrepayContext context) {
        String openid = context.channelParam(WechatPayChannelParams.OPENID);
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(ResultCode.WECHAT_PAY_OPENID_REQUIRED, "缺少付款人 openid");
        }
        return openid.trim();
    }

    private static PaymentClientInvokeParams toClientInvokeParams(WechatPayRequestPaymentParams params) {
        return new PaymentClientInvokeParams(
                params.timeStamp(),
                params.nonceStr(),
                params.packageValue(),
                params.signType(),
                params.paySign());
    }

    private static PaymentNotifyResult toNotifyResult(WechatPayNotifyParseResult parsed, String rawPayload) {
        return new PaymentNotifyResult(
                parsed.outTradeNo(),
                parsed.transactionId(),
                WechatPayTradeStateMapper.toPaymentStatus(parsed.tradeState()),
                rawPayload);
    }

    private static PaymentQueryResult toQueryResult(WechatPayNotifyParseResult parsed) {
        return new PaymentQueryResult(
                parsed.outTradeNo(),
                parsed.transactionId(),
                WechatPayTradeStateMapper.toPaymentStatus(parsed.tradeState()),
                null);
    }
}
