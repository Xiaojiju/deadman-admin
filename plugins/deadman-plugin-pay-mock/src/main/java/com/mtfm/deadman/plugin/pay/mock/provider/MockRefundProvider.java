package com.mtfm.deadman.plugin.pay.mock.provider;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.mock.config.MockPayPluginProperties;
import com.mtfm.deadman.plugin.pay.mock.constant.MockPayProviderIds;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.refund.AbnormalRefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundProvider;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundQueryResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock 退款 Provider：本地/测试环境模拟退款申请、查退款与回调。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
public class MockRefundProvider implements RefundProvider {

    /** 与 Mock 支付 Provider 共用标识 */
    public static final String PROVIDER_ID = MockPayProviderIds.MOCK;

    private static final Pattern JSON_STRING_FIELD = Pattern.compile("\"([a-z_]+)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern JSON_INT_FIELD = Pattern.compile("\"([a-z_]+)\"\\s*:\\s*(\\d+)");

    private final MockPayPluginProperties properties;

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
    public boolean autoCompleteAfterRefund() {
        return properties.isAutoCompleteOnRefund();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResult createRefund(RefundContext context, String outRefundNo) {
        log.info(
                "Mock 退款申请：outRefundNo={}, outTradeNo={}, amount={}, autoComplete={}",
                outRefundNo,
                context.getOutTradeNo(),
                context.getAmountRefund(),
                properties.isAutoCompleteOnRefund());
        String channelRefundId = "mock_rf_" + UUID.randomUUID().toString().replace("-", "");
        String transactionId = StringUtils.hasText(context.getChannelTransactionId())
                ? context.getChannelTransactionId()
                : "mock_tx_" + UUID.randomUUID().toString().replace("-", "");
        return new RefundResult(
                outRefundNo,
                context.getOutTradeNo(),
                channelRefundId,
                transactionId,
                context.getAmountRefund(),
                PaymentRefundStatus.PROCESSING,
                "Mock 退款账户",
                null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 接受简化 JSON：
     * {@code {"out_refund_no":"...","out_trade_no":"...","status":"SUCCESS","refund":100}}
     */
    @Override
    public RefundNotifyResult parseRefundNotify(ChannelNotifyContext context) {
        String outRefundNo = readJsonStringField(context.rawBody(), "out_refund_no");
        String outTradeNo = readJsonStringField(context.rawBody(), "out_trade_no");
        String status = readJsonStringField(context.rawBody(), "status");
        if (!StringUtils.hasText(outRefundNo) || !StringUtils.hasText(status)) {
            throw new BusinessException(
                    ResultCode.PAY_REFUND_NOTIFY_PARSE_FAILED, "Mock 退款回调缺少 out_refund_no 或 status");
        }
        String channelRefundId = readJsonStringField(context.rawBody(), "refund_id");
        if (!StringUtils.hasText(channelRefundId)) {
            channelRefundId = "mock_rf_" + UUID.randomUUID().toString().replace("-", "");
        }
        String transactionId = readJsonStringField(context.rawBody(), "transaction_id");
        Integer amountRefund = readJsonIntField(context.rawBody(), "refund");
        String targetStatus = resolveStatus(status);
        log.info("Mock 退款回调：outRefundNo={}, status={}", outRefundNo, targetStatus);
        return new RefundNotifyResult(
                outRefundNo,
                outTradeNo,
                channelRefundId,
                transactionId,
                amountRefund,
                targetStatus,
                "Mock 退款账户",
                context.rawBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundQueryResult queryRefund(String outRefundNo) {
        log.info("Mock 查退款：outRefundNo={}, status={}", outRefundNo, PaymentRefundStatus.SUCCESS);
        return new RefundQueryResult(
                outRefundNo,
                null,
                "mock_rf_" + UUID.randomUUID().toString().replace("-", ""),
                "mock_tx_" + UUID.randomUUID().toString().replace("-", ""),
                null,
                PaymentRefundStatus.SUCCESS,
                "Mock 退款账户",
                null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 异常退款受理后回到 PROCESSING，后续可由回调/查询推进到终态。
     */
    @Override
    public RefundResult createAbnormalRefund(AbnormalRefundContext context) {
        log.info(
                "Mock 异常退款：outRefundNo={}, channelRefundId={}, receiveType={}",
                context.getOutRefundNo(),
                context.getChannelRefundId(),
                context.getReceiveType());
        String receiveAccount = StringUtils.hasText(context.getReceiveType())
                ? context.getReceiveType().trim()
                : "Mock 商户结算账户";
        return new RefundResult(
                context.getOutRefundNo(),
                null,
                context.getChannelRefundId(),
                "mock_tx_" + UUID.randomUUID().toString().replace("-", ""),
                null,
                PaymentRefundStatus.PROCESSING,
                receiveAccount,
                null);
    }

    private static String resolveStatus(String status) {
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "SUCCESS", "CLOSED", "PROCESSING", "ABNORMAL" -> normalized;
            default -> throw new BusinessException(
                    ResultCode.PAY_REFUND_NOTIFY_PARSE_FAILED, "Mock 退款回调 status 无效：" + status);
        };
    }

    private static String readJsonStringField(String json, String fieldName) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        Matcher matcher = JSON_STRING_FIELD.matcher(json);
        while (matcher.find()) {
            if (fieldName.equals(matcher.group(1))) {
                return matcher.group(2);
            }
        }
        return null;
    }

    private static Integer readJsonIntField(String json, String fieldName) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        Matcher matcher = JSON_INT_FIELD.matcher(json);
        while (matcher.find()) {
            if (fieldName.equals(matcher.group(1))) {
                return Integer.valueOf(matcher.group(2));
            }
        }
        return null;
    }
}
