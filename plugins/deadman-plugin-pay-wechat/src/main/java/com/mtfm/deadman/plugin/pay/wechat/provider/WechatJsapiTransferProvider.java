package com.mtfm.deadman.plugin.pay.wechat.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.constant.TransferBillStatus;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferContext;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferProvider;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferQueryResult;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferParseResult;

import lombok.RequiredArgsConstructor;

/**
 * 微信小程序商家转账 Provider（与 JSAPI 支付共用商户与 AppId 绑定）。
 */
@Component
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-jsapi",
        name = "enabled",
        havingValue = "true")
@RequiredArgsConstructor
public class WechatJsapiTransferProvider implements TransferProvider {

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
    public String payPlatform() {
        return PaymentPlatform.WECHAT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransferResult createTransfer(TransferContext context, String outBillNo) {
        WechatPayProviderBindingProperties binding = requireBinding();
        if (!StringUtils.hasText(binding.getAppId())) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信 JSAPI AppId 未配置");
        }
        WechatTransferParseResult parsed = wechatPayApiGateway.createTransfer(new WechatTransferCommand(
                binding.getAppId(),
                outBillNo,
                context.getTransferSceneId(),
                context.getOpenid(),
                context.getUserName(),
                context.getAmountCents(),
                context.getTransferRemark(),
                binding.getTransferNotifyUrl()));
        return toTransferResult(parsed, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransferNotifyResult parseTransferNotify(ChannelNotifyContext context) {
        WechatTransferParseResult parsed = wechatPayApiGateway.parseTransferNotify(context);
        return new TransferNotifyResult(
                parsed.outBillNo(),
                parsed.channelBillNo(),
                parsed.amountCents(),
                normalizeStatus(parsed.state()),
                parsed.failReason(),
                context.rawBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransferQueryResult queryTransfer(String outBillNo) {
        WechatTransferParseResult parsed = wechatPayApiGateway.queryTransferByOutBillNo(outBillNo);
        return new TransferQueryResult(
                parsed.outBillNo(),
                parsed.channelBillNo(),
                parsed.amountCents(),
                normalizeStatus(parsed.state()),
                parsed.packageInfo(),
                parsed.failReason(),
                null);
    }

    /**
     * Mock 网关下自动查单完成。
     */
    @Override
    public boolean autoCompleteAfterTransfer() {
        return wechatPayPluginProperties.shouldUseMock();
    }

    private WechatPayProviderBindingProperties requireBinding() {
        WechatPayProviderBindingProperties binding = wechatPayPluginProperties.providerBinding(PROVIDER_ID);
        if (!binding.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "微信 JSAPI Provider 未启用");
        }
        return binding;
    }

    private static TransferResult toTransferResult(WechatTransferParseResult parsed, String rawPayload) {
        return new TransferResult(
                parsed.outBillNo(),
                parsed.channelBillNo(),
                normalizeStatus(parsed.state()),
                parsed.packageInfo(),
                parsed.failReason(),
                rawPayload);
    }

    private static String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return TransferBillStatus.PROCESSING;
        }
        return switch (status.trim().toUpperCase()) {
            case "ACCEPTED" -> TransferBillStatus.ACCEPTED;
            case "PROCESSING" -> TransferBillStatus.PROCESSING;
            case "WAIT_USER_CONFIRM" -> TransferBillStatus.WAIT_USER_CONFIRM;
            case "TRANSFERING", "TRANSFERRING" -> TransferBillStatus.TRANSFERING;
            case "SUCCESS" -> TransferBillStatus.SUCCESS;
            case "FAIL", "FAILED" -> TransferBillStatus.FAIL;
            case "CANCELLED", "CANCELED", "CANCELING", "CANCELLING" -> TransferBillStatus.CANCELLED;
            default -> throw new BusinessException(
                    ResultCode.PAY_TRANSFER_NOTIFY_PARSE_FAILED, "未知微信转账状态：" + status);
        };
    }
}
