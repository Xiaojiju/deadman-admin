package com.mtfm.deadman.plugin.pay.mock.provider;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentMethod;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.mock.constant.MockPayProviderIds;
import com.mtfm.deadman.plugin.pay.spi.PaymentClientInvokeParams;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.PaymentQueryResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock 支付 Provider：本地/测试环境模拟预下单、查单与回调，无需真实渠道。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
public class MockPaymentProvider implements PaymentProvider {

    /** Provider 标识，与 deadman.plugin.pay.default-provider 可对齐为 mock */
    public static final String PROVIDER_ID = MockPayProviderIds.MOCK;

    private static final Pattern JSON_STRING_FIELD = Pattern.compile("\"([a-z_]+)\"\\s*:\\s*\"([^\"]*)\"");

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
    public String payMethod() {
        return PaymentMethod.MOCK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context, String outTradeNo) {
        log.info(
                "Mock 支付预下单：outTradeNo={}, bizOrderNo={}, amount={}",
                outTradeNo,
                context.getBizOrderNo(),
                context.getAmountTotal());
        String prepayId = "mock_prepay_" + UUID.randomUUID().toString().replace("-", "");
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        PaymentClientInvokeParams clientInvokeParams =
                new PaymentClientInvokeParams(timeStamp, nonceStr, "prepay_id=" + prepayId, "MOCK", "mock_pay_sign");
        return new PaymentPrepayResult(outTradeNo, PROVIDER_ID, prepayId, clientInvokeParams);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 接受简化 JSON：{@code {"out_trade_no":"...","transaction_id":"...","status":"SUCCESS"}}
     */
    @Override
    public PaymentNotifyResult parseNotify(PaymentNotifyContext context) {
        String outTradeNo = readJsonStringField(context.rawBody(), "out_trade_no");
        String transactionId = readJsonStringField(context.rawBody(), "transaction_id");
        String status = readJsonStringField(context.rawBody(), "status");
        if (!StringUtils.hasText(outTradeNo) || !StringUtils.hasText(status)) {
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "Mock 回调缺少 out_trade_no 或 status");
        }
        if (!StringUtils.hasText(transactionId)) {
            transactionId = "mock_tx_" + UUID.randomUUID().toString().replace("-", "");
        }
        String targetStatus = resolveTargetStatus(status);
        log.info("Mock 支付回调：outTradeNo={}, status={}", outTradeNo, targetStatus);
        return new PaymentNotifyResult(outTradeNo, transactionId, targetStatus, context.rawBody());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 模式查单默认返回支付成功，便于定时同步或手动 sync 完成支付。
     */
    @Override
    public PaymentQueryResult queryOrder(String outTradeNo) {
        String transactionId = "mock_tx_" + UUID.randomUUID().toString().replace("-", "");
        log.info("Mock 支付查单：outTradeNo={}, status={}", outTradeNo, PaymentOrderStatus.SUCCESS);
        return new PaymentQueryResult(outTradeNo, transactionId, PaymentOrderStatus.SUCCESS, null);
    }

    private static String resolveTargetStatus(String status) {
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "SUCCESS", "NOT_PAY", "CLOSED", "REFUND" -> normalized;
            case "NOTPAY" -> PaymentOrderStatus.NOT_PAY;
            default -> throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "Mock 回调 status 无效：" + status);
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
}
