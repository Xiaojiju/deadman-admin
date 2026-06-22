package com.mtfm.deadman.plugin.pay.wechat.client;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock 微信支付网关，用于测试环境无商户号时验收预下单与回调链路。
 */
@Slf4j
public class MockWechatPayApiGateway implements WechatPayApiGateway {

    private static final Pattern JSON_STRING_FIELD =
            Pattern.compile("\"([a-z_]+)\"\\s*:\\s*\"([^\"]*)\"");

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayJsapiPrepayResult createJsapiPrepay(WechatJsapiPrepayCommand command) {
        log.info(
                "Mock 微信预下单：outTradeNo={}, amount={}, openid={}, appId={}",
                command.outTradeNo(),
                command.amountTotal(),
                command.openid(),
                command.appId());
        String prepayId = "mock_prepay_" + UUID.randomUUID().toString().replace("-", "");
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String packageValue = "prepay_id=" + prepayId;
        WechatPayRequestPaymentParams params =
                new WechatPayRequestPaymentParams(timeStamp, nonceStr, packageValue, "RSA", "mock_pay_sign");
        return new WechatPayJsapiPrepayResult(prepayId, params);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 模式接受简化 JSON：{@code {"out_trade_no":"...","transaction_id":"...","trade_state":"SUCCESS"}}
     */
    @Override
    public WechatPayNotifyParseResult parseNotify(PaymentNotifyContext context) {
        String outTradeNo = readJsonStringField(context.rawBody(), "out_trade_no");
        String transactionId = readJsonStringField(context.rawBody(), "transaction_id");
        String tradeState = readJsonStringField(context.rawBody(), "trade_state");
        if (!StringUtils.hasText(outTradeNo) || !StringUtils.hasText(tradeState)) {
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "Mock 回调缺少 out_trade_no 或 trade_state");
        }
        if (!StringUtils.hasText(transactionId)) {
            transactionId = "mock_tx_" + UUID.randomUUID().toString().replace("-", "");
        }
        log.info("Mock 微信支付回调：outTradeNo={}, tradeState={}", outTradeNo, tradeState);
        return new WechatPayNotifyParseResult(outTradeNo, transactionId, tradeState);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 模式：单号包含 {@code PAID} 时返回 SUCCESS，否则返回 NOTPAY。
     */
    @Override
    public WechatPayNotifyParseResult queryOrderByOutTradeNo(String outTradeNo) {
        String tradeState = outTradeNo != null && outTradeNo.contains("PAID") ? "SUCCESS" : "NOTPAY";
        String transactionId = "SUCCESS".equals(tradeState)
                ? "mock_tx_" + UUID.randomUUID().toString().replace("-", "")
                : null;
        log.info("Mock 微信查单：outTradeNo={}, tradeState={}", outTradeNo, tradeState);
        return new WechatPayNotifyParseResult(outTradeNo, transactionId, tradeState);
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
