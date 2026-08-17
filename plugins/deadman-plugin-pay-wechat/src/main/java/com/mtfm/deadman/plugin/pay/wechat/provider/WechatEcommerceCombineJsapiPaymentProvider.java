package com.mtfm.deadman.plugin.pay.wechat.provider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentMethod;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentChannelExtra;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentClientInvokeParams;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentCombineSubOrder;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayChannelParams;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayTradeStateMapper;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombineJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombinePrepayResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombineSubOrderCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;

import lombok.RequiredArgsConstructor;

/**
 * 微信收付通合单 JSAPI 支付 Provider，负责合单预下单与合单回调/查单解析。
 */
@Component
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-ecommerce-combine-jsapi",
        name = "enabled",
        havingValue = "true")
@RequiredArgsConstructor
public class WechatEcommerceCombineJsapiPaymentProvider implements PaymentProvider {

    /** Provider 标识 */
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
        List<WechatEcommerceCombineSubOrderCommand> subOrders = toSubOrderCommands(context);
        WechatEcommerceCombinePrepayResult prepayResult =
                wechatPayApiGateway.createEcommerceCombineJsapiPrepay(new WechatEcommerceCombineJsapiPrepayCommand(
                        requireAppId(binding),
                        wechatPayPluginProperties.resolvePartnerMchid(),
                        outTradeNo,
                        openid,
                        binding.getNotifyUrl(),
                        subOrders,
                        null));
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
    public PaymentNotifyResult parseNotify(ChannelNotifyContext context) {
        WechatPayNotifyParseResult parsed = wechatPayApiGateway.parseEcommerceCombineNotify(context);
        return toNotifyResult(parsed, context.rawBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentQueryResult queryOrder(String outTradeNo) {
        WechatPayNotifyParseResult parsed = wechatPayApiGateway.queryEcommerceCombineOrder(outTradeNo);
        return toQueryResult(parsed);
    }

    private WechatPayProviderBindingProperties requireBinding() {
        WechatPayProviderBindingProperties binding = wechatPayPluginProperties.providerBinding(PROVIDER_ID);
        if (!binding.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信收付通合单 JSAPI Provider 未启用");
        }
        return binding;
    }

    private static String requireAppId(WechatPayProviderBindingProperties binding) {
        if (!StringUtils.hasText(binding.getAppId())) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信收付通合单 AppId 未配置");
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

    private static List<WechatEcommerceCombineSubOrderCommand> toSubOrderCommands(PaymentPrepayContext context) {
        List<PaymentCombineSubOrder> subOrders = context.getSubOrders();
        if (subOrders == null || subOrders.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收付通合单缺少子单列表 subOrders");
        }
        List<WechatEcommerceCombineSubOrderCommand> commands = new ArrayList<>(subOrders.size());
        for (PaymentCombineSubOrder sub : subOrders) {
            if (sub == null || !StringUtils.hasText(sub.subMchid()) || !StringUtils.hasText(sub.outTradeNo())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "收付通合单子单缺少 subMchid 或 outTradeNo");
            }
            commands.add(new WechatEcommerceCombineSubOrderCommand(
                    sub.subMchid().trim(),
                    sub.outTradeNo().trim(),
                    StringUtils.hasText(sub.description()) ? sub.description() : context.getDescription(),
                    sub.amountTotal(),
                    sub.attach(),
                    sub.profitSharing()));
        }
        return commands;
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
                parsed.amountTotal(),
                rawPayload);
    }

    private static PaymentQueryResult toQueryResult(WechatPayNotifyParseResult parsed) {
        return new PaymentQueryResult(
                parsed.outTradeNo(),
                parsed.transactionId(),
                WechatPayTradeStateMapper.toPaymentStatus(parsed.tradeState()),
                parsed.amountTotal(),
                null);
    }
}
