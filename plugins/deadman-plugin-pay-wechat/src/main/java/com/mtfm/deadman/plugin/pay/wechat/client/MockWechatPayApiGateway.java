package com.mtfm.deadman.plugin.pay.wechat.client;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.constant.PayScoreOrderState;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.constant.TransferBillStatus;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScorePermissionResponse;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScoreServiceOrderResponse;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAbnormalRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAddProfitSharingReceiverCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombineJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombinePrepayResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatMediaUploadResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCancelCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCompleteCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScorePermissionCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingFinishCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnResult;
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
     */
    @Override
    public WechatRefundParseResult queryEcommerceRefundByOutRefundNo(String outRefundNo, String subMchid) {
        log.info(
                "Mock 收付通查退款：outRefundNo={}, subMchid={}, status={}",
                outRefundNo,
                subMchid,
                PaymentRefundStatus.SUCCESS);
        return new WechatRefundParseResult(
                outRefundNo,
                null,
                "mock_ec_rf_" + UUID.randomUUID().toString().replace("-", ""),
                "mock_tx_" + UUID.randomUUID().toString().replace("-", ""),
                null,
                PaymentRefundStatus.SUCCESS,
                "支付用户零钱");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult parseEcommerceRefundNotify(ChannelNotifyContext context) {
        log.info("Mock 收付通退款回调");
        return new WechatRefundParseResult(
                "mock_out_refund_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                "mock_out_trade",
                "mock_ec_rf_" + UUID.randomUUID().toString().replace("-", ""),
                "mock_tx_" + UUID.randomUUID().toString().replace("-", ""),
                1,
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

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatEcommerceCombinePrepayResult createEcommerceCombineJsapiPrepay(
            WechatEcommerceCombineJsapiPrepayCommand command) {
        log.info(
                "Mock 收付通合单预下单：combineOutTradeNo={}, subOrderCount={}",
                command.combineOutTradeNo(),
                command.subOrders() == null ? 0 : command.subOrders().size());
        String prepayId = "MOCK_prepay_" + UUID.randomUUID().toString().replace("-", "");
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        WechatPayRequestPaymentParams params = new WechatPayRequestPaymentParams(
                timeStamp, nonceStr, "prepay_id=" + prepayId, "RSA", "MOCK_pay_sign");
        return new WechatEcommerceCombinePrepayResult(prepayId, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayNotifyParseResult queryEcommerceCombineOrder(String combineOutTradeNo) {
        String tradeState = combineOutTradeNo != null && combineOutTradeNo.contains("PAID") ? "SUCCESS" : "NOTPAY";
        String transactionId = "SUCCESS".equals(tradeState)
                ? "MOCK_tx_" + UUID.randomUUID().toString().replace("-", "")
                : null;
        log.info("Mock 收付通合单查单：combineOutTradeNo={}, tradeState={}", combineOutTradeNo, tradeState);
        return new WechatPayNotifyParseResult(combineOutTradeNo, transactionId, tradeState, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayNotifyParseResult parseEcommerceCombineNotify(ChannelNotifyContext context) {
        String combineOutTradeNo = readJsonStringField(context.rawBody(), "combine_out_trade_no");
        if (!StringUtils.hasText(combineOutTradeNo)) {
            combineOutTradeNo = readJsonStringField(context.rawBody(), "out_trade_no");
        }
        String tradeState = readJsonStringField(context.rawBody(), "trade_state");
        if (!StringUtils.hasText(combineOutTradeNo) || !StringUtils.hasText(tradeState)) {
            throw new BusinessException(
                    ResultCode.PAY_NOTIFY_PARSE_FAILED, "Mock 合单回调缺少 combine_out_trade_no 或 trade_state");
        }
        String transactionId = readJsonStringField(context.rawBody(), "transaction_id");
        if (!StringUtils.hasText(transactionId)) {
            transactionId = "MOCK_tx_" + UUID.randomUUID().toString().replace("-", "");
        }
        Integer amountTotal = readJsonIntField(context.rawBody(), "total");
        log.info("Mock 收付通合单回调：combineOutTradeNo={}, tradeState={}", combineOutTradeNo, tradeState);
        return new WechatPayNotifyParseResult(combineOutTradeNo, transactionId, tradeState, amountTotal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingCreateResult createEcommerceProfitSharing(WechatProfitSharingCreateCommand command) {
        log.info(
                "Mock 收付通请求分账：outOrderNo={}, transactionId={}",
                command.outOrderNo(),
                command.transactionId());
        return new WechatProfitSharingCreateResult(
                command.subMchid(),
                command.transactionId(),
                command.outOrderNo(),
                "MOCK_ps_" + UUID.randomUUID().toString().replace("-", ""),
                "PROCESSING");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingQueryResult queryEcommerceProfitSharing(
            String subMchid, String transactionId, String outOrderNo) {
        log.info("Mock 收付通分账查询：outOrderNo={}", outOrderNo);
        return new WechatProfitSharingQueryResult(
                subMchid,
                transactionId,
                outOrderNo,
                "MOCK_ps_" + UUID.randomUUID().toString().replace("-", ""),
                "FINISHED",
                "[]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingCreateResult finishEcommerceProfitSharing(WechatProfitSharingFinishCommand command) {
        log.info("Mock 收付通完结分账：outOrderNo={}", command.outOrderNo());
        return new WechatProfitSharingCreateResult(
                command.subMchid(),
                command.transactionId(),
                command.outOrderNo(),
                "MOCK_ps_" + UUID.randomUUID().toString().replace("-", ""),
                "FINISHED");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingReturnResult returnEcommerceProfitSharing(WechatProfitSharingReturnCommand command) {
        log.info("Mock 收付通分账回退：outReturnNo={}", command.outReturnNo());
        return new WechatProfitSharingReturnResult(
                command.subMchid(),
                command.orderId(),
                command.outOrderNo(),
                command.outReturnNo(),
                command.returnMchid(),
                command.amount(),
                "SUCCESS");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEcommerceProfitSharingReceiver(WechatAddProfitSharingReceiverCommand command) {
        log.info("Mock 收付通添加分账接收方：account={}", command.account());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatEcommerceApplymentResult createEcommerceApplyment(WechatEcommerceApplymentCommand command) {
        log.info("Mock 收付通进件：outRequestNo={}", command.outRequestNo());
        return new WechatEcommerceApplymentResult(
                "MOCK_apply_" + UUID.randomUUID().toString().replace("-", ""),
                command.outRequestNo(),
                null,
                "APPLYMENT_STATE_AUDITING",
                "https://mock.weixin.qq.com/sign/" + UUID.randomUUID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatEcommerceApplymentResult queryEcommerceApplyment(String outRequestNo) {
        log.info("Mock 收付通进件查询：outRequestNo={}", outRequestNo);
        return new WechatEcommerceApplymentResult(
                "MOCK_apply_" + UUID.randomUUID().toString().replace("-", ""),
                outRequestNo,
                "MOCK_submch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10),
                "APPLYMENT_STATE_FINISHED",
                null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatMediaUploadResult uploadMedia(String fileName, byte[] content) {
        log.info("Mock 微信媒体上传：fileName={}, size={}", fileName, content == null ? 0 : content.length);
        return new WechatMediaUploadResult("MOCK_media_" + UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult createEcommerceRefund(WechatEcommerceRefundCommand command) {
        log.info(
                "Mock 收付通退款：outRefundNo={}, subMchid={}, amount={}",
                command.outRefundNo(),
                command.subMchid(),
                command.refundAmount());
        return new WechatRefundParseResult(
                command.outRefundNo(),
                command.outTradeNo(),
                "MOCK_rf_" + UUID.randomUUID().toString().replace("-", ""),
                StringUtils.hasText(command.transactionId())
                        ? command.transactionId()
                        : "MOCK_tx_" + UUID.randomUUID().toString().replace("-", ""),
                command.refundAmount(),
                PaymentRefundStatus.PROCESSING,
                "支付用户零钱");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse createPayScoreServiceOrder(WechatPayScoreCreateCommand cmd) {
        log.info(
                "Mock 微信支付分创建服务订单：outOrderNo={}, serviceId={}, openid={}",
                cmd.outOrderNo(),
                cmd.serviceId(),
                cmd.openid());
        PayScoreServiceOrderResponse response = new PayScoreServiceOrderResponse();
        response.setOutOrderNo(cmd.outOrderNo());
        response.setOrderId("mock_ps_" + UUID.randomUUID().toString().replace("-", ""));
        response.setServiceId(cmd.serviceId());
        response.setAppid(cmd.appId());
        response.setOpenid(cmd.openid());
        response.setState(PayScoreOrderState.CREATED);
        response.setStateDescription("商户已创建服务订单");
        response.setPackageInfo("mock_package_" + cmd.outOrderNo());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse queryPayScoreServiceOrder(
            String appId, String serviceId, String outOrderNo) {
        log.info("Mock 微信支付分查询服务订单：outOrderNo={}, state=DOING", outOrderNo);
        PayScoreServiceOrderResponse response = new PayScoreServiceOrderResponse();
        response.setOutOrderNo(outOrderNo);
        response.setOrderId("mock_ps_" + UUID.randomUUID().toString().replace("-", ""));
        response.setServiceId(serviceId);
        response.setAppid(appId);
        response.setState(PayScoreOrderState.DOING);
        response.setStateDescription("服务订单进行中");
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse cancelPayScoreServiceOrder(WechatPayScoreCancelCommand cmd) {
        log.info("Mock 微信支付分取消服务订单：outOrderNo={}, reason={}", cmd.outOrderNo(), cmd.reason());
        PayScoreServiceOrderResponse response = new PayScoreServiceOrderResponse();
        response.setOutOrderNo(cmd.outOrderNo());
        response.setOrderId("mock_ps_" + UUID.randomUUID().toString().replace("-", ""));
        response.setServiceId(cmd.serviceId());
        response.setAppid(cmd.appId());
        response.setState(PayScoreOrderState.REVOKED);
        response.setStateDescription("商户取消服务订单");
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse completePayScoreServiceOrder(WechatPayScoreCompleteCommand cmd) {
        log.info(
                "Mock 微信支付分完结服务订单：outOrderNo={}, totalAmount={}",
                cmd.outOrderNo(),
                cmd.totalAmountCents());
        PayScoreServiceOrderResponse response = new PayScoreServiceOrderResponse();
        response.setOutOrderNo(cmd.outOrderNo());
        response.setOrderId("mock_ps_" + UUID.randomUUID().toString().replace("-", ""));
        response.setServiceId(cmd.serviceId());
        response.setAppid(cmd.appId());
        response.setState(PayScoreOrderState.DONE);
        response.setStateDescription("服务订单已完成");
        response.setTotalAmount(cmd.totalAmountCents());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScorePermissionResponse createPayScorePermission(WechatPayScorePermissionCommand cmd) {
        log.info("Mock 微信支付分创建授权：serviceId={}, authorizationCode={}", cmd.serviceId(), cmd.authorizationCode());
        PayScorePermissionResponse response = new PayScorePermissionResponse();
        response.setServiceId(cmd.serviceId());
        response.setAppid(cmd.appId());
        response.setAuthorizationCode(cmd.authorizationCode());
        response.setAuthorizationState("AVAILABLE");
        response.setApplyPermissionsToken("mock_token_" + UUID.randomUUID().toString().replace("-", ""));
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScorePermissionResponse queryPayScorePermissionByOpenid(
            String appId, String serviceId, String openid) {
        log.info("Mock 微信支付分查询授权：openid={}, serviceId={}", openid, serviceId);
        PayScorePermissionResponse response = new PayScorePermissionResponse();
        response.setServiceId(serviceId);
        response.setAppid(appId);
        response.setOpenid(openid);
        response.setAuthorizationState("AVAILABLE");
        response.setOpenOrOrderId(openid);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminatePayScorePermissionByOpenid(
            String appId, String serviceId, String openid, String reason) {
        log.info(
                "Mock 微信支付分解除授权：openid={}, serviceId={}, reason={}", openid, serviceId, reason);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Mock 模式接受简化 JSON：
     * {@code {"out_order_no":"...","state":"DONE","total_amount":100}}
     */
    @Override
    public WechatPayScoreParseResult parsePayScoreNotify(ChannelNotifyContext context) {
        String outOrderNo = readJsonStringField(context.rawBody(), "out_order_no");
        String state = readJsonStringField(context.rawBody(), "state");
        if (!StringUtils.hasText(outOrderNo) || !StringUtils.hasText(state)) {
            throw new BusinessException(
                    ResultCode.PAY_SCORE_NOTIFY_PARSE_FAILED, "Mock 支付分回调缺少 out_order_no 或 state");
        }
        String orderId = readJsonStringField(context.rawBody(), "order_id");
        if (!StringUtils.hasText(orderId)) {
            orderId = "mock_ps_" + UUID.randomUUID().toString().replace("-", "");
        }
        Long totalAmount = null;
        Integer totalAmountInt = readJsonIntField(context.rawBody(), "total_amount");
        if (totalAmountInt != null) {
            totalAmount = totalAmountInt.longValue();
        }
        String openid = readJsonStringField(context.rawBody(), "openid");
        String eventType = readJsonStringField(context.rawBody(), "event_type");
        log.info("Mock 微信支付分回调：outOrderNo={}, state={}", outOrderNo, state);
        return new WechatPayScoreParseResult(
                outOrderNo, orderId, state, null, totalAmount, openid, null, eventType);
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
