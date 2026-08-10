package com.mtfm.deadman.plugin.pay.mock.provider;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.constant.TransferBillStatus;
import com.mtfm.deadman.plugin.pay.mock.config.MockPayPluginProperties;
import com.mtfm.deadman.plugin.pay.mock.constant.MockPayProviderIds;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferContext;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferProvider;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferQueryResult;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock 商家转账 Provider。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
public class MockTransferProvider implements TransferProvider {

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
    public String payPlatform() {
        return PaymentPlatform.MOCK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean autoCompleteAfterTransfer() {
        return properties.isAutoCompleteOnTransfer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransferResult createTransfer(TransferContext context, String outBillNo) {
        log.info(
                "Mock 商家转账：outBillNo={}, amount={}, openid={}, autoComplete={}",
                outBillNo,
                context.getAmountCents(),
                context.getOpenid(),
                properties.isAutoCompleteOnTransfer());
        return new TransferResult(
                outBillNo,
                "mock_tf_" + UUID.randomUUID().toString().replace("-", ""),
                TransferBillStatus.ACCEPTED,
                null,
                null,
                null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransferNotifyResult parseTransferNotify(ChannelNotifyContext context) {
        String outBillNo = readJsonStringField(context.rawBody(), "out_bill_no");
        String status = readJsonStringField(context.rawBody(), "status");
        if (!StringUtils.hasText(status)) {
            status = readJsonStringField(context.rawBody(), "state");
        }
        if (!StringUtils.hasText(outBillNo) || !StringUtils.hasText(status)) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_NOTIFY_PARSE_FAILED, "Mock 转账回调缺少 out_bill_no 或 status/state");
        }
        String channelBillNo = readJsonStringField(context.rawBody(), "transfer_bill_no");
        if (!StringUtils.hasText(channelBillNo)) {
            channelBillNo = "mock_tf_" + UUID.randomUUID().toString().replace("-", "");
        }
        Integer amount = readJsonIntField(context.rawBody(), "transfer_amount");
        String targetStatus = resolveStatus(status);
        log.info("Mock 商家转账回调：outBillNo={}, status={}", outBillNo, targetStatus);
        return new TransferNotifyResult(
                outBillNo,
                channelBillNo,
                amount == null ? null : amount.longValue(),
                targetStatus,
                readJsonStringField(context.rawBody(), "fail_reason"),
                context.rawBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransferQueryResult queryTransfer(String outBillNo) {
        log.info("Mock 查转账：outBillNo={}, status=SUCCESS", outBillNo);
        return new TransferQueryResult(
                outBillNo,
                "mock_tf_" + UUID.randomUUID().toString().replace("-", ""),
                null,
                TransferBillStatus.SUCCESS,
                null,
                null,
                null);
    }

    private static String resolveStatus(String status) {
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "ACCEPTED",
                    "PROCESSING",
                    "WAIT_USER_CONFIRM",
                    "TRANSFERING",
                    "SUCCESS",
                    "FAIL",
                    "CANCELLED" -> normalized;
            case "TRANSFERRING" -> TransferBillStatus.TRANSFERING;
            case "FAILED" -> TransferBillStatus.FAIL;
            default -> throw new BusinessException(
                    ResultCode.PAY_TRANSFER_NOTIFY_PARSE_FAILED, "Mock 转账回调 status 无效：" + status);
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
