package com.mtfm.deadman.plugin.pay.wechat.client;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.AbnormalRefundReceiveType;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayNotifyHeaders;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayChannelErrorClassifier;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayNotifyHeaderUtils;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAbnormalRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferBillNotification;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferParseResult;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.http.HttpHeaders;
import com.wechat.pay.java.core.http.HttpMethod;
import com.wechat.pay.java.core.http.HttpRequest;
import com.wechat.pay.java.core.http.HttpResponse;
import com.wechat.pay.java.core.http.JsonRequestBody;
import com.wechat.pay.java.core.http.MediaType;
import com.wechat.pay.java.core.http.UrlEncoder;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.GsonUtil;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信支付 APIv3 真实网关实现。
 */
@Slf4j
public class WechatPayApiGatewayImpl implements WechatPayApiGateway {

    private static final String ABNORMAL_REFUND_URL_TEMPLATE = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/%s/apply-abnormal-refund";

    private static final String TRANSFER_BILL_URL = "https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills";

    private static final String TRANSFER_QUERY_BY_OUT_BILL_NO_URL_TEMPLATE = "https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills/out-bill-no/%s";

    private final WechatPayPluginProperties properties;
    private final Config config;
    private final JsapiService jsapiService;
    private final RefundService refundService;
    private final HttpClient httpClient;
    private final NotificationParser notificationParser;
    /** 构造期缓存的商户私钥，避免每次预下单读盘 */
    private final PrivateKey merchantPrivateKey;

    /**
     * 构造真实微信支付网关。
     *
     * @param properties 插件配置
     */
    public WechatPayApiGatewayImpl(WechatPayPluginProperties properties) {
        this.properties = properties;
        this.config = buildConfig(properties);
        this.jsapiService = new JsapiService.Builder().config(config).build();
        this.refundService = new RefundService.Builder().config(config).build();
        this.httpClient = new DefaultHttpClientBuilder().config(config).build();
        this.notificationParser = new NotificationParser((RSAAutoCertificateConfig) config);
        try {
            this.merchantPrivateKey = loadPrivateKey(properties.getPrivateKeyPath());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "加载商户私钥失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayJsapiPrepayResult createJsapiPrepay(WechatJsapiPrepayCommand command) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(command.appId());
        request.setMchid(properties.getMchId());
        request.setDescription(command.description());
        request.setOutTradeNo(command.outTradeNo());
        request.setNotifyUrl(command.notifyUrl());
        Amount amount = new Amount();
        amount.setTotal(command.amountTotal());
        amount.setCurrency("CNY");
        request.setAmount(amount);
        Payer payer = new Payer();
        payer.setOpenid(command.openid());
        request.setPayer(payer);
        try {
            PrepayResponse response = jsapiService.prepay(request);
            String prepayId = response.getPrepayId();
            WechatPayRequestPaymentParams params = signRequestPayment(command.appId(), prepayId);
            return new WechatPayJsapiPrepayResult(prepayId, params);
        } catch (RuntimeException ex) {
            log.warn("微信预下单失败：outTradeNo={}", command.outTradeNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_PREPAY_FAILED, "微信预下单失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayNotifyParseResult parseNotify(ChannelNotifyContext context) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SERIAL))
                .nonce(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.NONCE))
                .signature(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SIGNATURE))
                .timestamp(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.TIMESTAMP))
                .body(context.rawBody())
                .build();
        try {
            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            return toParseResult(transaction);
        } catch (RuntimeException ex) {
            log.warn("微信支付回调验签或解密失败", ex);
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "微信支付回调验签或解密失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayNotifyParseResult queryOrderByOutTradeNo(String outTradeNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(properties.getMchId());
        request.setOutTradeNo(outTradeNo);
        try {
            Transaction transaction = jsapiService.queryOrderByOutTradeNo(request);
            return toParseResult(transaction);
        } catch (RuntimeException ex) {
            log.warn("微信查单失败：outTradeNo={}", outTradeNo, ex);
            throw new BusinessException(ResultCode.PAY_QUERY_FAILED, "微信查单失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult createRefund(WechatRefundCommand command) {
        CreateRequest request = new CreateRequest();
        if (StringUtils.hasText(command.channelTransactionId())) {
            request.setTransactionId(command.channelTransactionId());
        } else {
            request.setOutTradeNo(command.outTradeNo());
        }
        request.setOutRefundNo(command.outRefundNo());
        if (StringUtils.hasText(command.reason())) {
            request.setReason(command.reason());
        }
        if (StringUtils.hasText(command.notifyUrl())) {
            request.setNotifyUrl(command.notifyUrl());
        }
        AmountReq amount = new AmountReq();
        amount.setRefund((long) command.amountRefund());
        amount.setTotal((long) command.amountTotal());
        amount.setCurrency(StringUtils.hasText(command.currency()) ? command.currency() : "CNY");
        request.setAmount(amount);
        try {
            Refund refund = refundService.create(request);
            return toRefundParseResult(refund);
        } catch (com.wechat.pay.java.core.exception.ServiceException ex) {
            if (WechatPayChannelErrorClassifier.isClearReject(ex)) {
                log.warn(
                        "微信退款申请被明确拒绝：outRefundNo={}, errorCode={}",
                        command.outRefundNo(),
                        ex.getErrorCode(),
                        ex);
                throw new BusinessException(
                        ResultCode.WECHAT_PAY_REFUND_FAILED,
                        "微信退款申请失败：" + ex.getErrorCode(),
                        ex);
            }
            // SYSTEM_ERROR / 限频等：不确定是否已受理，上层保留 PROCESSING 并查单
            log.warn(
                    "微信退款申请结果不确定：outRefundNo={}, errorCode={}",
                    command.outRefundNo(),
                    ex.getErrorCode(),
                    ex);
            throw new BusinessException(ResultCode.PAY_REFUND_FAILED, "微信退款调用异常，结果不确定", ex);
        } catch (RuntimeException ex) {
            log.warn("微信退款申请异常（结果不确定）：outRefundNo={}", command.outRefundNo(), ex);
            throw new BusinessException(ResultCode.PAY_REFUND_FAILED, "微信退款调用异常，结果不确定", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult parseRefundNotify(ChannelNotifyContext context) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SERIAL))
                .nonce(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.NONCE))
                .signature(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SIGNATURE))
                .timestamp(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.TIMESTAMP))
                .body(context.rawBody())
                .build();
        try {
            RefundNotification notification = notificationParser.parse(requestParam, RefundNotification.class);
            return toRefundNotifyParseResult(notification);
        } catch (RuntimeException ex) {
            log.warn("微信退款回调验签或解密失败", ex);
            throw new BusinessException(ResultCode.PAY_REFUND_NOTIFY_PARSE_FAILED, "微信退款回调验签或解密失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult queryRefundByOutRefundNo(String outRefundNo) {
        QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(outRefundNo);
        try {
            Refund refund = refundService.queryByOutRefundNo(request);
            return toRefundParseResult(refund);
        } catch (RuntimeException ex) {
            log.warn("微信查退款失败：outRefundNo={}", outRefundNo, ex);
            throw new BusinessException(ResultCode.PAY_REFUND_QUERY_FAILED, "微信查退款失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult createAbnormalRefund(WechatAbnormalRefundCommand command) {
        if (!StringUtils.hasText(command.channelRefundId()) || !StringUtils.hasText(command.outRefundNo())) {
            throw new BusinessException(ResultCode.WECHAT_PAY_ABNORMAL_REFUND_FAILED,
                    "异常退款缺少 refund_id 或 out_refund_no");
        }
        String receiveType = StringUtils.hasText(command.receiveType())
                ? command.receiveType().trim()
                : AbnormalRefundReceiveType.MERCHANT_BANK_CARD;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("out_refund_no", command.outRefundNo());
        body.put("type", receiveType);
        if (AbnormalRefundReceiveType.USER_BANK_CARD.equals(receiveType)) {
            body.put("bank_type", command.bankType());
            body.put("bank_account", command.bankAccount());
            body.put("real_name", command.realName());
        }
        String url = String.format(
                ABNORMAL_REFUND_URL_TEMPLATE, UrlEncoder.urlEncode(command.channelRefundId().trim()));
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        headers.addHeader("Content-Type", MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.POST)
                .url(url)
                .headers(headers)
                .body(new JsonRequestBody.Builder().body(GsonUtil.toJson(body)).build())
                .build();
        try {
            HttpResponse<Refund> response = httpClient.execute(httpRequest, Refund.class);
            return toRefundParseResult(response.getServiceResponse());
        } catch (com.wechat.pay.java.core.exception.ServiceException ex) {
            if (WechatPayChannelErrorClassifier.isClearReject(ex)) {
                log.warn(
                        "微信异常退款被明确拒绝：outRefundNo={}, refundId={}, errorCode={}",
                        command.outRefundNo(),
                        command.channelRefundId(),
                        ex.getErrorCode(),
                        ex);
                throw new BusinessException(
                        ResultCode.WECHAT_PAY_ABNORMAL_REFUND_FAILED,
                        "微信异常退款申请失败：" + ex.getErrorCode(),
                        ex);
            }
            log.warn(
                    "微信异常退款结果不确定：outRefundNo={}, refundId={}, errorCode={}",
                    command.outRefundNo(),
                    command.channelRefundId(),
                    ex.getErrorCode(),
                    ex);
            throw new BusinessException(ResultCode.PAY_ABNORMAL_REFUND_FAILED, "微信异常退款调用异常，结果不确定", ex);
        } catch (RuntimeException ex) {
            log.warn(
                    "微信异常退款申请异常（结果不确定）：outRefundNo={}, refundId={}",
                    command.outRefundNo(),
                    command.channelRefundId(),
                    ex);
            throw new BusinessException(ResultCode.PAY_ABNORMAL_REFUND_FAILED, "微信异常退款调用异常，结果不确定", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public WechatTransferParseResult createTransfer(WechatTransferCommand command) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("appid", command.appId());
        body.put("out_bill_no", command.outBillNo());
        body.put("transfer_scene_id", command.transferSceneId());
        body.put("openid", command.openid());
        body.put("transfer_amount", command.transferAmount());
        body.put("transfer_remark", command.transferRemark());
        if (StringUtils.hasText(command.userName())) {
            body.put("user_name", command.userName());
        }
        if (StringUtils.hasText(command.notifyUrl())) {
            body.put("notify_url", command.notifyUrl());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        headers.addHeader("Content-Type", MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.POST)
                .url(TRANSFER_BILL_URL)
                .headers(headers)
                .body(new JsonRequestBody.Builder().body(GsonUtil.toJson(body)).build())
                .build();
        try {
            HttpResponse<Map> response = httpClient.execute(httpRequest, Map.class);
            return toTransferParseResult(response.getServiceResponse());
        } catch (com.wechat.pay.java.core.exception.ServiceException ex) {
            if (WechatPayChannelErrorClassifier.isClearReject(ex)) {
                log.warn(
                        "微信商家转账申请被明确拒绝：outBillNo={}, errorCode={}",
                        command.outBillNo(),
                        ex.getErrorCode(),
                        ex);
                throw new BusinessException(
                        ResultCode.WECHAT_PAY_TRANSFER_FAILED,
                        "微信商家转账申请失败：" + ex.getErrorCode(),
                        ex);
            }
            // SYSTEM_ERROR / FREQUENCY_LIMITED / ALREADY_EXISTS 等：保留 PROCESSING 并查单
            log.warn(
                    "微信商家转账申请结果不确定：outBillNo={}, errorCode={}",
                    command.outBillNo(),
                    ex.getErrorCode(),
                    ex);
            throw new BusinessException(ResultCode.PAY_TRANSFER_FAILED, "微信商家转账调用异常，结果不确定", ex);
        } catch (RuntimeException ex) {
            // 超时/网络等：不确定渠道是否已受理，上层应保留 PROCESSING 并查单
            log.warn("微信商家转账申请异常（结果不确定）：outBillNo={}", command.outBillNo(), ex);
            throw new BusinessException(ResultCode.PAY_TRANSFER_FAILED, "微信商家转账调用异常，结果不确定", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatTransferParseResult parseTransferNotify(ChannelNotifyContext context) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SERIAL))
                .nonce(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.NONCE))
                .signature(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SIGNATURE))
                .timestamp(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.TIMESTAMP))
                .body(context.rawBody())
                .build();
        try {
            WechatTransferBillNotification notification = notificationParser.parse(requestParam,
                    WechatTransferBillNotification.class);
            return new WechatTransferParseResult(
                    notification.getOutBillNo(),
                    notification.getTransferBillNo(),
                    notification.getTransferAmount(),
                    notification.getState(),
                    notification.getPackageInfo(),
                    notification.getFailReason());
        } catch (RuntimeException ex) {
            log.warn("微信商家转账回调验签或解密失败", ex);
            throw new BusinessException(ResultCode.PAY_TRANSFER_NOTIFY_PARSE_FAILED, "微信商家转账回调验签或解密失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public WechatTransferParseResult queryTransferByOutBillNo(String outBillNo) {
        String url = String.format(TRANSFER_QUERY_BY_OUT_BILL_NO_URL_TEMPLATE, UrlEncoder.urlEncode(outBillNo));
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.GET)
                .url(url)
                .headers(headers)
                .build();
        try {
            HttpResponse<Map> response = httpClient.execute(httpRequest, Map.class);
            return toTransferParseResult(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信商家转账查单失败：outBillNo={}", outBillNo, ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_TRANSFER_FAILED, "微信商家转账查单失败");
        }
    }

    private static WechatTransferParseResult toTransferParseResult(Map<?, ?> response) {
        if (response == null) {
            throw new BusinessException(ResultCode.WECHAT_PAY_TRANSFER_FAILED, "微信商家转账响应为空");
        }
        String outBillNo = asString(response.get("out_bill_no"));
        String channelBillNo = asString(response.get("transfer_bill_no"));
        String state = asString(response.get("state"));
        String packageInfo = asString(response.get("package_info"));
        String failReason = asString(response.get("fail_reason"));
        Long amount = asLong(response.get("transfer_amount"));
        return new WechatTransferParseResult(outBillNo, channelBillNo, amount, state, packageInfo, failReason);
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static WechatPayNotifyParseResult toParseResult(Transaction transaction) {
        Integer amountTotal = null;
        if (transaction.getAmount() != null && transaction.getAmount().getTotal() != null) {
            amountTotal = transaction.getAmount().getTotal();
        }
        return new WechatPayNotifyParseResult(
                transaction.getOutTradeNo(),
                transaction.getTransactionId(),
                transaction.getTradeState().name(),
                amountTotal);
    }

    private static WechatRefundParseResult toRefundParseResult(Refund refund) {
        Integer amountRefund = null;
        if (refund.getAmount() != null && refund.getAmount().getRefund() != null) {
            amountRefund = refund.getAmount().getRefund().intValue();
        }
        String status = refund.getStatus() == null ? null : refund.getStatus().name();
        return new WechatRefundParseResult(
                refund.getOutRefundNo(),
                refund.getOutTradeNo(),
                refund.getRefundId(),
                refund.getTransactionId(),
                amountRefund,
                status,
                refund.getUserReceivedAccount());
    }

    private static WechatRefundParseResult toRefundNotifyParseResult(RefundNotification notification) {
        Integer amountRefund = null;
        if (notification.getAmount() != null && notification.getAmount().getRefund() != null) {
            amountRefund = notification.getAmount().getRefund().intValue();
        }
        String status = notification.getRefundStatus() == null ? null : notification.getRefundStatus().name();
        return new WechatRefundParseResult(
                notification.getOutRefundNo(),
                notification.getOutTradeNo(),
                notification.getRefundId(),
                notification.getTransactionId(),
                amountRefund,
                status,
                notification.getUserReceivedAccount());
    }

    private WechatPayRequestPaymentParams signRequestPayment(String appId, String prepayId) {
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String packageValue = "prepay_id=" + prepayId;
        String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageValue + "\n";
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(merchantPrivateKey);
            signature.update(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String paySign = Base64.getEncoder().encodeToString(signature.sign());
            return new WechatPayRequestPaymentParams(timeStamp, nonceStr, packageValue, "RSA", paySign);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.WECHAT_PAY_PREPAY_FAILED, "生成支付签名失败");
        }
    }

    private static Config buildConfig(WechatPayPluginProperties properties) {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(properties.getMchId())
                .privateKeyFromPath(properties.getPrivateKeyPath())
                .merchantSerialNumber(properties.getMerchantSerialNo())
                .apiV3Key(properties.getApiV3Key())
                .build();
    }

    private static PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        if (!StringUtils.hasText(privateKeyPath)) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "商户私钥路径未配置");
        }
        java.nio.file.Path path = java.nio.file.Path.of(privateKeyPath);
        String pem = java.nio.file.Files.readString(path);
        String normalized = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
