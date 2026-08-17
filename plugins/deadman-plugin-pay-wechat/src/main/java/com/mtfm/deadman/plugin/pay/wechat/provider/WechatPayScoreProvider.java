package com.mtfm.deadman.plugin.pay.wechat.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCancelContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCompleteContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCompleteResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCreateContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCreateResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScorePermissionContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScorePermissionResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreProvider;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScorePermissionResponse;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScoreServiceOrderResponse;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCancelCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCompleteCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScorePermissionCommand;

import lombok.RequiredArgsConstructor;

/**
 * 微信支付分 Provider（创建/查询/取消/完结服务订单与授权）。
 */
@Component
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-payscore",
        name = "enabled",
        havingValue = "true")
@RequiredArgsConstructor
public class WechatPayScoreProvider implements PayScoreProvider {

    /** 支付分 Provider 标识 */
    public static final String PROVIDER_ID = WechatPayProviderIds.WECHAT_PAY_SCORE;

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
    public PayScoreCreateResult createServiceOrder(PayScoreCreateContext context, String outOrderNo) {
        WechatPayProviderBindingProperties binding = requireBinding();
        String serviceId = resolveServiceId(context == null ? null : context.getServiceId(), binding);
        String notifyUrl = context != null && StringUtils.hasText(context.getNotifyUrl())
                ? context.getNotifyUrl()
                : binding.getPayScoreNotifyUrl();
        PayScoreServiceOrderResponse response = wechatPayApiGateway.createPayScoreServiceOrder(
                new WechatPayScoreCreateCommand(
                        binding.getAppId(),
                        serviceId,
                        outOrderNo,
                        context == null ? null : context.getOpenid(),
                        context == null ? null : context.getServiceIntroduction(),
                        context == null ? null : context.getRiskFundName(),
                        context == null ? 0L : context.getRiskFundAmountCents(),
                        context == null ? null : context.getTimeRangeStartTime(),
                        context == null ? null : context.getTimeRangeEndTime(),
                        context == null ? null : context.getLocationName(),
                        notifyUrl,
                        context == null ? null : context.getAttach(),
                        context == null || context.isNeedUserConfirm()));
        return PayScoreCreateResult.builder()
                .outOrderNo(response.getOutOrderNo())
                .channelOrderId(response.getOrderId())
                .state(response.getState())
                .packageInfo(response.getPackageInfo())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreQueryResult queryServiceOrder(String outOrderNo, String serviceId) {
        WechatPayProviderBindingProperties binding = requireBinding();
        String resolvedServiceId = resolveServiceId(serviceId, binding);
        PayScoreServiceOrderResponse response = wechatPayApiGateway.queryPayScoreServiceOrder(
                binding.getAppId(), resolvedServiceId, outOrderNo);
        return PayScoreQueryResult.builder()
                .outOrderNo(response.getOutOrderNo())
                .channelOrderId(response.getOrderId())
                .state(response.getState())
                .stateDescription(response.getStateDescription())
                .totalAmountCents(response.getTotalAmount())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelServiceOrder(PayScoreCancelContext context, String outOrderNo) {
        WechatPayProviderBindingProperties binding = requireBinding();
        String appId = context != null && StringUtils.hasText(context.getAppId())
                ? context.getAppId()
                : binding.getAppId();
        String serviceId = resolveServiceId(context == null ? null : context.getServiceId(), binding);
        wechatPayApiGateway.cancelPayScoreServiceOrder(new WechatPayScoreCancelCommand(
                appId, serviceId, outOrderNo, context == null ? null : context.getReason()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreCompleteResult completeServiceOrder(PayScoreCompleteContext context, String outOrderNo) {
        WechatPayProviderBindingProperties binding = requireBinding();
        String serviceId = resolveServiceId(context == null ? null : context.getServiceId(), binding);
        PayScoreServiceOrderResponse response = wechatPayApiGateway.completePayScoreServiceOrder(
                new WechatPayScoreCompleteCommand(
                        binding.getAppId(),
                        serviceId,
                        outOrderNo,
                        context == null ? null : context.getPostPayments(),
                        context == null ? 0L : context.getTotalAmountCents(),
                        context == null ? null : context.getPostDiscounts(),
                        context == null ? null : context.getTimeRangeEndTime(),
                        context == null ? null : context.getCompleteTime()));
        return PayScoreCompleteResult.builder()
                .outOrderNo(response.getOutOrderNo())
                .channelOrderId(response.getOrderId())
                .state(response.getState())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScorePermissionResult createPermission(PayScorePermissionContext context) {
        WechatPayProviderBindingProperties binding = requireBinding();
        String serviceId = resolveServiceId(context == null ? null : context.getServiceId(), binding);
        String notifyUrl = context != null && StringUtils.hasText(context.getNotifyUrl())
                ? context.getNotifyUrl()
                : binding.getPayScoreNotifyUrl();
        PayScorePermissionResponse response = wechatPayApiGateway.createPayScorePermission(
                new WechatPayScorePermissionCommand(
                        binding.getAppId(),
                        serviceId,
                        context == null ? null : context.getAuthorizationCode(),
                        context == null ? null : context.getOpenid(),
                        notifyUrl));
        return toPermissionResult(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScorePermissionResult queryPermissionByOpenid(String openid, String serviceId) {
        WechatPayProviderBindingProperties binding = requireBinding();
        String resolvedServiceId = resolveServiceId(serviceId, binding);
        PayScorePermissionResponse response = wechatPayApiGateway.queryPayScorePermissionByOpenid(
                binding.getAppId(), resolvedServiceId, openid);
        return toPermissionResult(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminatePermissionByOpenid(String openid, String serviceId, String reason) {
        WechatPayProviderBindingProperties binding = requireBinding();
        String resolvedServiceId = resolveServiceId(serviceId, binding);
        wechatPayApiGateway.terminatePayScorePermissionByOpenid(
                binding.getAppId(), resolvedServiceId, openid, reason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreNotifyResult parseNotify(ChannelNotifyContext context) {
        WechatPayScoreParseResult parsed = wechatPayApiGateway.parsePayScoreNotify(context);
        return PayScoreNotifyResult.builder()
                .outOrderNo(parsed.outOrderNo())
                .channelOrderId(parsed.channelOrderId())
                .state(parsed.state())
                .eventType(parsed.eventType())
                .totalAmountCents(parsed.totalAmountCents())
                .openid(parsed.openid())
                .rawBody(context == null ? null : context.rawBody())
                .build();
    }

    private WechatPayProviderBindingProperties requireBinding() {
        WechatPayProviderBindingProperties binding = wechatPayPluginProperties.providerBinding(PROVIDER_ID);
        if (!binding.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信支付分 Provider 未启用");
        }
        if (!StringUtils.hasText(binding.getAppId())) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信支付分 AppId 未配置");
        }
        return binding;
    }

    private static String resolveServiceId(String contextServiceId, WechatPayProviderBindingProperties binding) {
        if (StringUtils.hasText(contextServiceId)) {
            return contextServiceId.trim();
        }
        if (StringUtils.hasText(binding.getServiceId())) {
            return binding.getServiceId().trim();
        }
        throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信支付分 serviceId 未配置");
    }

    private static PayScorePermissionResult toPermissionResult(PayScorePermissionResponse response) {
        String openOrOrderId = StringUtils.hasText(response.getOpenOrOrderId())
                ? response.getOpenOrOrderId()
                : response.getApplyPermissionsToken();
        return PayScorePermissionResult.builder()
                .serviceId(response.getServiceId())
                .openid(response.getOpenid())
                .authorizationCode(response.getAuthorizationCode())
                .authorizationState(response.getAuthorizationState())
                .openOrOrderId(openOrOrderId)
                .build();
    }
}
