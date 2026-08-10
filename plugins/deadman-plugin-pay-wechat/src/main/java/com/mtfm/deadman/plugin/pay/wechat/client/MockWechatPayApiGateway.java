package com.mtfm.deadman.plugin.pay.wechat.client;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.constant.TransferBillStatus;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAbnormalRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferParseResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock 微信支付网关，用于测试环境无商户号时验收预下单、退款与回调链路。
 */
@Slf4j
public class MockWechatPayApiGateway implements WechatPayApiGateway {

    private static final Pattern JSON_STRING_FIELD =
            Pattern.compile("\"([a-z_]+)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern JSON_INT_FIELD = Pattern.compile("\"([a-z_]+)\"\\s*:\\s*(\\d+)");

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
    public WechatPayNotifyParseResult parseNotify(ChannelNotifyContext context) {
        String outTradeNo = readJsonStringField(context.rawBody(), "out_trade_no");
        String transactionId = readJsonStringField(context.rawBody(), "transaction_id");
        String tradeState = readJsonStringField(context.rawBody(), "trade_state");
        if (!StringUtils.hasText(outTradeNo) || !StringUtils.hasText(tradeState)) {
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "Mock 回调缺少 out_trade_no 或 trade_state");
        }
        if (!StringUtils.hasText(transactionId)) {
            transactionId = "mock_tx_" + UUID.randomUUID().toString().replace("-", "");
        }
        Integer amountTotal = readJsonIntField(context.rawBody(), "total");
        log.info("Mock 微信支付回调：outTradeNo={}, tradeState={}", outTradeNo, tradeState);
        return new WechatPayNotifyParseResult(outTradeNo, transactionId, tradeState, amountTotal);
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
        return new WechatPayNotifyParseResult(outTradeNo, transactionId, tradeState, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult createRefund(WechatRefundCommand command) {
        log.info(
                "Mock 微信退款：outRefundNo={}, outTradeNo={}, amount={}",
                command.outRefundNo(),
                command.outTradeNo(),
                command.amountRefund());
        String channelRefundId = "mock_rf_" + UUID.randomUUID().toString().replace("-", "");
        String transactionId = StringUtils.hasText(command.channelTransactionId())
                ? command.channelTransactionId()
                : "mock_tx_" + UUID.randomUUID().toString().replace("-", "");
        return new WechatRefundParseResult(
                command.outRefundNo(),
                command.outTradeNo(),
                channelRefundId,
                transactionId,
                command.amountRefund(),
                PaymentRefundStatus.PROCESSING,
                "支付用户零钱");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 模式接受简化 JSON：
     * {@code {"out_refund_no":"...","out_trade_no":"...","refund_status":"SUCCESS","refund":100}}
     */
    @Override
    public WechatRefundParseResult parseRefundNotify(ChannelNotifyContext context) {
        String outRefundNo = readJsonStringField(context.rawBody(), "out_refund_no");
        String outTradeNo = readJsonStringField(context.rawBody(), "out_trade_no");
        String refundStatus = readJsonStringField(context.rawBody(), "refund_status");
        if (!StringUtils.hasText(outRefundNo) || !StringUtils.hasText(refundStatus)) {
            throw new BusinessException(
                    ResultCode.PAY_REFUND_NOTIFY_PARSE_FAILED, "Mock 退款回调缺少 out_refund_no 或 refund_status");
        }
        String channelRefundId = readJsonStringField(context.rawBody(), "refund_id");
        if (!StringUtils.hasText(channelRefundId)) {
            channelRefundId = "mock_rf_" + UUID.randomUUID().toString().replace("-", "");
        }
        String transactionId = readJsonStringField(context.rawBody(), "transaction_id");
        Integer amountRefund = readJsonIntField(context.rawBody(), "refund");
        log.info("Mock 微信退款回调：outRefundNo={}, status={}", outRefundNo, refundStatus);
        return new WechatRefundParseResult(
                outRefundNo,
                outTradeNo,
                channelRefundId,
                transactionId,
                amountRefund,
                refundStatus,
                "支付用户零钱");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 查退款默认返回 SUCCESS，便于同步补偿。
     */
    @Override
    public WechatRefundParseResult queryRefundByOutRefundNo(String outRefundNo) {
        log.info("Mock 微信查退款：outRefundNo={}, status={}", outRefundNo, PaymentRefundStatus.SUCCESS);
        return new WechatRefundParseResult(
                outRefundNo,
                null,
                "mock_rf_" + UUID.randomUUID().toString().replace("-", ""),
                "mock_tx_" + UUID.randomUUID().toString().replace("-", ""),
                null,
                PaymentRefundStatus.SUCCESS,
                "支付用户零钱");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 异常退款受理后回到 PROCESSING。
     */
    @Override
    public WechatRefundParseResult createAbnormalRefund(WechatAbnormalRefundCommand command) {
        log.info(
                "Mock 微信异常退款：outRefundNo={}, refundId={}, type={}",
                command.outRefundNo(),
                command.channelRefundId(),
                command.receiveType());
        return new WechatRefundParseResult(
                command.outRefundNo(),
                null,
                command.channelRefundId(),
                "mock_tx_" + UUID.randomUUID().toString().replace("-", ""),
                null,
                PaymentRefundStatus.PROCESSING,
                "商户结算银行账户");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatTransferParseResult createTransfer(WechatTransferCommand command) {
        log.info(
                "Mock 微信商家转账：outBillNo={}, amount={}, openid={}",
                command.outBillNo(),
                command.transferAmount(),
                command.openid());
        return new WechatTransferParseResult(
                command.outBillNo(),
                "mock_tf_" + UUID.randomUUID().toString().replace("-", ""),
                command.transferAmount(),
                TransferBillStatus.ACCEPTED,
                null,
                null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 模式接受简化 JSON：
     * {@code {"out_bill_no":"...","state":"SUCCESS","transfer_amount":100}}
     */
    @Override
    public WechatTransferParseResult parseTransferNotify(ChannelNotifyContext context) {
        String outBillNo = readJsonStringField(context.rawBody(), "out_bill_no");
        String state = readJsonStringField(context.rawBody(), "state");
        if (!StringUtils.hasText(outBillNo) || !StringUtils.hasText(state)) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_NOTIFY_PARSE_FAILED, "Mock 转账回调缺少 out_bill_no 或 state");
        }
        String channelBillNo = readJsonStringField(context.rawBody(), "transfer_bill_no");
        if (!StringUtils.hasText(channelBillNo)) {
            channelBillNo = "mock_tf_" + UUID.randomUUID().toString().replace("-", "");
        }
        Long amount = null;
        Integer amountInt = readJsonIntField(context.rawBody(), "transfer_amount");
        if (amountInt != null) {
            amount = amountInt.longValue();
        }
        String failReason = readJsonStringField(context.rawBody(), "fail_reason");
        log.info("Mock 微信商家转账回调：outBillNo={}, state={}", outBillNo, state);
        return new WechatTransferParseResult(outBillNo, channelBillNo, amount, state, null, failReason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatTransferParseResult queryTransferByOutBillNo(String outBillNo) {
        log.info("Mock 微信商家转账查单：outBillNo={}, state=SUCCESS", outBillNo);
        return new WechatTransferParseResult(
                outBillNo,
                "mock_tf_" + UUID.randomUUID().toString().replace("-", ""),
                null,
                TransferBillStatus.SUCCESS,
                null,
                null);
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
