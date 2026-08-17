package com.mtfm.deadman.plugin.pay.wechat.client;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.AbnormalRefundReceiveType;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScorePostPayment;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombineAmount;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombineJsapiPrepayRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombineOrderNotification;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombinePayerInfo;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombinePrepayResponse;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombineSceneInfo;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombineSettleInfo;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombineSubOrder;
import com.mtfm.deadman.plugin.pay.wechat.client.model.CombineSubOrderResult;
import com.mtfm.deadman.plugin.pay.wechat.client.model.DomesticAbnormalRefundRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.EcommerceApplymentRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.EcommerceApplymentResponse;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScoreCancelOrderRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScoreCompleteOrderRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScorePermissionRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScorePermissionResponse;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScoreServiceOrderRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScoreServiceOrderResponse;
import com.mtfm.deadman.plugin.pay.wechat.client.model.TransferBillRequest;
import com.mtfm.deadman.plugin.pay.wechat.client.model.TransferBillResponse;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayNotifyHeaders;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayChannelErrorClassifier;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayNotifyHeaderUtils;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAbnormalRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAddProfitSharingReceiverCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombineJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombinePrepayResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombineSubOrderCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceRefundNotification;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatMediaUploadResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCancelCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCompleteCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreNotification;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScorePermissionCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingFinishCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReceiverCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferBillNotification;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferParseResult;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.http.HttpHeaders;
import com.wechat.pay.java.core.http.HttpMethod;
import com.wechat.pay.java.core.http.HttpRequest;
import com.wechat.pay.java.core.http.HttpResponse;
import com.wechat.pay.java.core.http.JsonRequestBody;
import com.wechat.pay.java.core.http.MediaType;
import com.wechat.pay.java.core.http.UrlEncoder;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.GsonUtil;
import com.wechat.pay.java.service.ecommerceprofitsharing.EcommerceProfitSharingService;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.AddReceiverRequest;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.CreateOrderReceiver;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.CreateOrderRequest;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.CreateOrderResponse;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.CreateReturnOrderRequest;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.CreateReturnOrderResponse;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.FinishOrderRequest;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.FinishOrderResponse;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.QueryOrderRequest;
import com.wechat.pay.java.service.ecommerceprofitsharing.model.QueryOrderResponse;
import com.wechat.pay.java.service.ecommercerefund.EcommerceRefundService;
import com.wechat.pay.java.service.ecommercerefund.model.CreateRefundRequest;
import com.wechat.pay.java.service.ecommercerefund.model.QueryRefundByOutRefundNoRequest;
import com.wechat.pay.java.service.ecommercerefund.model.Refund4Create;
import com.wechat.pay.java.service.ecommercerefund.model.RefundReqAmount;
import com.wechat.pay.java.service.file.FileUploadService;
import com.wechat.pay.java.service.file.model.FileUploadResponse;
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
 * 微信支付 APIv3 真实网关实现（优先官方 SDK Service；无 Service 的 API 使用强类型模型 + HttpClient）。
 */
@Slf4j
public class WechatPayApiGatewayImpl implements WechatPayApiGateway {

    private static final String ABNORMAL_REFUND_URL_TEMPLATE = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/%s/apply-abnormal-refund";

    private static final String TRANSFER_BILL_URL = "https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills";

    private static final String TRANSFER_QUERY_BY_OUT_BILL_NO_URL_TEMPLATE = "https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills/out-bill-no/%s";

    private static final String COMBINE_JSAPI_URL = "https://api.mch.weixin.qq.com/v3/combine-transactions/jsapi";

    private static final String COMBINE_QUERY_URL_TEMPLATE = "https://api.mch.weixin.qq.com/v3/combine-transactions/out-trade-no/%s";

    private static final String ECOMMERCE_APPLYMENTS_URL = "https://api.mch.weixin.qq.com/v3/ecommerce/applyments/";

    private static final String ECOMMERCE_APPLYMENT_QUERY_URL_TEMPLATE = "https://api.mch.weixin.qq.com/v3/ecommerce/applyments/out-request-no/%s";

    /** 商户进件等业务媒体上传（图片 MediaID） */
    private static final String MERCHANT_MEDIA_UPLOAD_URL = "https://api.mch.weixin.qq.com/v3/merchant/media/upload";

    /** 微信媒体上传允许的图片后缀 */
    private static final Set<String> MEDIA_IMAGE_SUFFIXES = Set.of("jpg", "jpeg", "png", "bmp");

    /** 图片上限 5MB（官方文档） */
    private static final int MEDIA_MAX_BYTES = 5 * 1024 * 1024;

    private static final String PAY_SCORE_SERVICE_ORDER_URL =
            "https://api.mch.weixin.qq.com/v3/payscore/serviceorder";

    private static final String PAY_SCORE_SERVICE_ORDER_CANCEL_URL_TEMPLATE =
            "https://api.mch.weixin.qq.com/v3/payscore/serviceorder/%s/cancel";

    private static final String PAY_SCORE_SERVICE_ORDER_COMPLETE_URL_TEMPLATE =
            "https://api.mch.weixin.qq.com/v3/payscore/serviceorder/%s/complete";

    private static final String PAY_SCORE_PERMISSIONS_URL = "https://api.mch.weixin.qq.com/v3/payscore/permissions";

    private static final String PAY_SCORE_PERMISSION_BY_OPENID_URL_TEMPLATE =
            "https://api.mch.weixin.qq.com/v3/payscore/permissions/openid/%s";

    private static final String PAY_SCORE_PERMISSION_TERMINATE_URL_TEMPLATE =
            "https://api.mch.weixin.qq.com/v3/payscore/permissions/openid/%s/terminate";

    private final WechatPayPluginProperties properties;
    private final Config config;
    private final JsapiService jsapiService;
    private final RefundService refundService;
    private final EcommerceProfitSharingService ecommerceProfitSharingService;
    private final EcommerceRefundService ecommerceRefundService;
    private final FileUploadService fileUploadService;
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
        this.ecommerceProfitSharingService = new EcommerceProfitSharingService.Builder().config(config).build();
        this.ecommerceRefundService = new EcommerceRefundService.Builder().config(config).build();
        this.httpClient = new DefaultHttpClientBuilder().config(config).build();
        this.fileUploadService = new FileUploadService.Builder().httpClient(httpClient).build();
        this.notificationParser = new NotificationParser((NotificationConfig) config);
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
    public WechatRefundParseResult queryEcommerceRefundByOutRefundNo(String outRefundNo, String subMchid) {
        if (!StringUtils.hasText(outRefundNo) || !StringUtils.hasText(subMchid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收付通查退款缺少 outRefundNo 或 subMchid");
        }
        QueryRefundByOutRefundNoRequest request = new QueryRefundByOutRefundNoRequest();
        request.setOutRefundNo(outRefundNo.trim());
        request.setSubMchid(subMchid.trim());
        try {
            com.wechat.pay.java.service.ecommercerefund.model.Refund refund =
                    ecommerceRefundService.queryRefundByOutRefundNo(request);
            return toEcommerceRefundParseResult(refund);
        } catch (RuntimeException ex) {
            log.warn("微信收付通查退款失败：outRefundNo={}, subMchid={}", outRefundNo, subMchid, ex);
            throw new BusinessException(ResultCode.PAY_REFUND_QUERY_FAILED, "微信收付通查退款失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult parseEcommerceRefundNotify(ChannelNotifyContext context) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SERIAL))
                .nonce(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.NONCE))
                .signature(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SIGNATURE))
                .timestamp(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.TIMESTAMP))
                .body(context.rawBody())
                .build();
        try {
            WechatEcommerceRefundNotification notification =
                    notificationParser.parse(requestParam, WechatEcommerceRefundNotification.class);
            return toEcommerceRefundNotifyParseResult(notification);
        } catch (RuntimeException ex) {
            log.warn("微信收付通退款回调验签或解密失败", ex);
            throw new BusinessException(ResultCode.PAY_REFUND_NOTIFY_PARSE_FAILED, "微信收付通退款回调验签或解密失败");
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
        DomesticAbnormalRefundRequest body = new DomesticAbnormalRefundRequest();
        body.setOutRefundNo(command.outRefundNo());
        body.setType(receiveType);
        if (AbnormalRefundReceiveType.USER_BANK_CARD.equals(receiveType)) {
            body.setBankType(command.bankType());
            body.setBankAccount(command.bankAccount());
            body.setRealName(command.realName());
        }
        String url = String.format(
                ABNORMAL_REFUND_URL_TEMPLATE, UrlEncoder.urlEncode(command.channelRefundId().trim()));
        try {
            HttpResponse<Refund> response = postJson(url, body, Refund.class);
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
    public WechatTransferParseResult createTransfer(WechatTransferCommand command) {
        TransferBillRequest body = new TransferBillRequest();
        body.setAppid(command.appId());
        body.setOutBillNo(command.outBillNo());
        body.setTransferSceneId(command.transferSceneId());
        body.setOpenid(command.openid());
        body.setTransferAmount(command.transferAmount());
        body.setTransferRemark(command.transferRemark());
        if (StringUtils.hasText(command.userName())) {
            body.setUserName(command.userName());
        }
        if (StringUtils.hasText(command.notifyUrl())) {
            body.setNotifyUrl(command.notifyUrl());
        }
        try {
            HttpResponse<TransferBillResponse> response = postJson(TRANSFER_BILL_URL, body, TransferBillResponse.class);
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
            log.warn(
                    "微信商家转账申请结果不确定：outBillNo={}, errorCode={}",
                    command.outBillNo(),
                    ex.getErrorCode(),
                    ex);
            throw new BusinessException(ResultCode.PAY_TRANSFER_FAILED, "微信商家转账调用异常，结果不确定", ex);
        } catch (RuntimeException ex) {
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
            HttpResponse<TransferBillResponse> response = httpClient.execute(httpRequest, TransferBillResponse.class);
            return toTransferParseResult(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信商家转账查单失败：outBillNo={}", outBillNo, ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_TRANSFER_FAILED, "微信商家转账查单失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatEcommerceCombinePrepayResult createEcommerceCombineJsapiPrepay(
            WechatEcommerceCombineJsapiPrepayCommand command) {
        if (command.subOrders() == null || command.subOrders().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收付通合单子单列表不能为空");
        }
        CombineJsapiPrepayRequest body = new CombineJsapiPrepayRequest();
        body.setCombineAppid(command.combineAppid());
        body.setCombineMchid(command.combineMchid());
        body.setCombineOutTradeNo(command.combineOutTradeNo());
        CombinePayerInfo payerInfo = new CombinePayerInfo();
        payerInfo.setOpenid(command.openid());
        body.setCombinePayerInfo(payerInfo);
        List<CombineSubOrder> subOrders = new ArrayList<>();
        for (WechatEcommerceCombineSubOrderCommand sub : command.subOrders()) {
            CombineSubOrder subOrder = new CombineSubOrder();
            subOrder.setMchid(sub.subMchid());
            subOrder.setOutTradeNo(sub.outTradeNo());
            subOrder.setDescription(sub.description());
            if (StringUtils.hasText(sub.attach())) {
                subOrder.setAttach(sub.attach());
            }
            CombineAmount amount = new CombineAmount();
            amount.setTotalAmount(sub.amountTotal());
            amount.setCurrency("CNY");
            subOrder.setAmount(amount);
            CombineSettleInfo settleInfo = new CombineSettleInfo();
            settleInfo.setProfitSharing(sub.profitSharing());
            subOrder.setSettleInfo(settleInfo);
            subOrders.add(subOrder);
        }
        body.setSubOrders(subOrders);
        body.setNotifyUrl(command.notifyUrl());
        if (StringUtils.hasText(command.payerClientIp())) {
            CombineSceneInfo sceneInfo = new CombineSceneInfo();
            sceneInfo.setPayerClientIp(command.payerClientIp());
            body.setSceneInfo(sceneInfo);
        }
        try {
            HttpResponse<CombinePrepayResponse> response = postJson(COMBINE_JSAPI_URL, body,
                    CombinePrepayResponse.class);
            CombinePrepayResponse resp = response.getServiceResponse();
            String prepayId = resp == null ? null : resp.getPrepayId();
            if (!StringUtils.hasText(prepayId)) {
                throw new BusinessException(ResultCode.WECHAT_PAY_PREPAY_FAILED, "收付通合单预下单未返回 prepay_id");
            }
            WechatPayRequestPaymentParams params = signRequestPayment(command.combineAppid(), prepayId);
            return new WechatEcommerceCombinePrepayResult(prepayId, params);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("收付通合单预下单失败：combineOutTradeNo={}", command.combineOutTradeNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_PREPAY_FAILED, "收付通合单预下单失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayNotifyParseResult queryEcommerceCombineOrder(String combineOutTradeNo) {
        String url = String.format(COMBINE_QUERY_URL_TEMPLATE, UrlEncoder.urlEncode(combineOutTradeNo));
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.GET)
                .url(url)
                .headers(headers)
                .build();
        try {
            HttpResponse<CombineOrderNotification> response = httpClient.execute(httpRequest,
                    CombineOrderNotification.class);
            return toCombineParseResult(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("收付通合单查单失败：combineOutTradeNo={}", combineOutTradeNo, ex);
            throw new BusinessException(ResultCode.PAY_QUERY_FAILED, "收付通合单查单失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayNotifyParseResult parseEcommerceCombineNotify(ChannelNotifyContext context) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SERIAL))
                .nonce(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.NONCE))
                .signature(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SIGNATURE))
                .timestamp(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.TIMESTAMP))
                .body(context.rawBody())
                .build();
        try {
            CombineOrderNotification decrypted = notificationParser.parse(requestParam, CombineOrderNotification.class);
            return toCombineParseResult(decrypted);
        } catch (RuntimeException ex) {
            log.warn("收付通合单支付回调验签或解密失败", ex);
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "收付通合单支付回调验签或解密失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingCreateResult createEcommerceProfitSharing(WechatProfitSharingCreateCommand command) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSubMchid(command.subMchid());
        request.setTransactionId(command.transactionId());
        request.setOutOrderNo(command.outOrderNo());
        request.setFinish(command.finish());
        List<CreateOrderReceiver> receivers = new ArrayList<>();
        if (command.receivers() != null) {
            for (WechatProfitSharingReceiverCommand receiver : command.receivers()) {
                CreateOrderReceiver item = new CreateOrderReceiver();
                item.setType(receiver.type());
                item.setReceiverAccount(receiver.account());
                item.setAmount((long) receiver.amount());
                item.setDescription(receiver.description());
                receivers.add(item);
            }
        }
        request.setReceivers(receivers);
        if (properties.getEcommerce() != null
                && StringUtils.hasText(properties.getEcommerce().getProfitSharingNotifyUrl())) {
            request.setNotifyUrl(properties.getEcommerce().getProfitSharingNotifyUrl());
        }
        try {
            CreateOrderResponse response = ecommerceProfitSharingService.createOrder(request);
            return toProfitSharingCreateResult(response);
        } catch (RuntimeException ex) {
            log.warn("收付通请求分账失败：outOrderNo={}", command.outOrderNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_PREPAY_FAILED, "收付通请求分账失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingQueryResult queryEcommerceProfitSharing(
            String subMchid, String transactionId, String outOrderNo) {
        QueryOrderRequest request = new QueryOrderRequest();
        request.setSubMchid(subMchid);
        request.setTransactionId(transactionId);
        request.setOutOrderNo(outOrderNo);
        try {
            QueryOrderResponse resp = ecommerceProfitSharingService.queryOrder(request);
            return new WechatProfitSharingQueryResult(
                    resp.getSubMchid(),
                    resp.getTransactionId(),
                    resp.getOutOrderNo(),
                    resp.getOrderId(),
                    resp.getStatus(),
                    resp.getReceivers() == null ? null : GsonUtil.toJson(resp.getReceivers()));
        } catch (RuntimeException ex) {
            log.warn("收付通分账查询失败：outOrderNo={}", outOrderNo, ex);
            throw new BusinessException(ResultCode.PAY_QUERY_FAILED, "收付通分账查询失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingCreateResult finishEcommerceProfitSharing(WechatProfitSharingFinishCommand command) {
        FinishOrderRequest request = new FinishOrderRequest();
        request.setSubMchid(command.subMchid());
        request.setTransactionId(command.transactionId());
        request.setOutOrderNo(command.outOrderNo());
        request.setDescription(command.description());
        try {
            FinishOrderResponse response = ecommerceProfitSharingService.finishOrder(request);
            return new WechatProfitSharingCreateResult(
                    response.getSubMchid(),
                    response.getTransactionId(),
                    response.getOutOrderNo(),
                    response.getOrderId(),
                    null);
        } catch (RuntimeException ex) {
            log.warn("收付通完结分账失败：outOrderNo={}", command.outOrderNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_PREPAY_FAILED, "收付通完结分账失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatProfitSharingReturnResult returnEcommerceProfitSharing(WechatProfitSharingReturnCommand command) {
        CreateReturnOrderRequest request = new CreateReturnOrderRequest();
        request.setSubMchid(command.subMchid());
        if (StringUtils.hasText(command.orderId())) {
            request.setOrderId(command.orderId());
        }
        if (StringUtils.hasText(command.outOrderNo())) {
            request.setOutOrderNo(command.outOrderNo());
        }
        request.setOutReturnNo(command.outReturnNo());
        request.setReturnMchid(command.returnMchid());
        request.setAmount((long) command.amount());
        request.setDescription(command.description());
        try {
            CreateReturnOrderResponse resp = ecommerceProfitSharingService.createReturnOrder(request);
            Integer amount = resp.getAmount() == null ? null : resp.getAmount().intValue();
            return new WechatProfitSharingReturnResult(
                    resp.getSubMchid(),
                    resp.getOrderId(),
                    resp.getOutOrderNo(),
                    resp.getOutReturnNo(),
                    resp.getReturnMchid(),
                    amount,
                    resp.getResult());
        } catch (RuntimeException ex) {
            log.warn("收付通分账回退失败：outReturnNo={}", command.outReturnNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_REFUND_FAILED, "收付通分账回退失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEcommerceProfitSharingReceiver(WechatAddProfitSharingReceiverCommand command) {
        AddReceiverRequest request = new AddReceiverRequest();
        request.setAppid(command.appid());
        request.setType(command.type());
        request.setAccount(command.account());
        request.setRelationType(command.relationType());
        try {
            ecommerceProfitSharingService.addReceiver(request);
        } catch (RuntimeException ex) {
            log.warn("收付通添加分账接收方失败：account={}", command.account(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "收付通添加分账接收方失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatEcommerceApplymentResult createEcommerceApplyment(WechatEcommerceApplymentCommand command) {
        EcommerceApplymentRequest body = new EcommerceApplymentRequest();
        body.setOutRequestNo(command.outRequestNo());
        body.setOrganizationType(command.organizationType());
        body.setMerchantShortname(command.merchantShortname());

        if (StringUtils.hasText(command.businessLicenseCopy())
                || StringUtils.hasText(command.businessLicenseNumber())) {
            EcommerceApplymentRequest.BusinessLicenseInfo license = new EcommerceApplymentRequest.BusinessLicenseInfo();
            license.setBusinessLicenseCopy(command.businessLicenseCopy());
            license.setBusinessLicenseNumber(command.businessLicenseNumber());
            license.setMerchantName(command.businessLicenseMerchantName());
            license.setLegalPerson(command.businessLicenseLegalPerson());
            body.setBusinessLicenseInfo(license);
        }

        EcommerceApplymentRequest.IdCardInfo idCardInfo = new EcommerceApplymentRequest.IdCardInfo();
        idCardInfo.setIdCardCopy(command.idCardCopy());
        idCardInfo.setIdCardNational(command.idCardNational());
        idCardInfo.setIdCardName(command.idCardName());
        idCardInfo.setIdCardNumber(command.idCardNumber());
        idCardInfo.setIdCardValidTimeBegin(command.idCardValidTimeBegin());
        idCardInfo.setIdCardValidTime(command.idCardValidTime());
        body.setIdCardInfo(idCardInfo);

        EcommerceApplymentRequest.AccountInfo accountInfo = new EcommerceApplymentRequest.AccountInfo();
        accountInfo.setBankAccountType(command.bankAccountType());
        accountInfo.setAccountBank(command.accountBank());
        accountInfo.setAccountName(command.accountName());
        accountInfo.setAccountNumber(command.accountNumber());
        body.setAccountInfo(accountInfo);

        EcommerceApplymentRequest.ContactInfo contactInfo = new EcommerceApplymentRequest.ContactInfo();
        contactInfo.setContactType(command.contactType());
        contactInfo.setContactName(command.contactName());
        contactInfo.setMobilePhone(command.mobilePhone());
        if (StringUtils.hasText(command.contactIdCardNumber())) {
            contactInfo.setContactIdCardNumber(command.contactIdCardNumber());
        }
        body.setContactInfo(contactInfo);

        EcommerceApplymentRequest.SalesSceneInfo salesSceneInfo = new EcommerceApplymentRequest.SalesSceneInfo();
        salesSceneInfo.setStoreName(command.storeName());
        if (StringUtils.hasText(command.storeUrl())) {
            salesSceneInfo.setStoreUrl(command.storeUrl());
        }
        if (StringUtils.hasText(command.storeQrCode())) {
            salesSceneInfo.setStoreQrCode(command.storeQrCode());
        }
        body.setSalesSceneInfo(salesSceneInfo);

        if (properties.getEcommerce() != null
                && StringUtils.hasText(properties.getEcommerce().getApplymentNotifyUrl())) {
            body.setNotifyUrl(properties.getEcommerce().getApplymentNotifyUrl());
        }
        try {
            HttpResponse<EcommerceApplymentResponse> response = postJson(ECOMMERCE_APPLYMENTS_URL, body,
                    EcommerceApplymentResponse.class);
            return toApplymentResult(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("收付通进件申请失败：outRequestNo={}", command.outRequestNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "收付通进件申请失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatEcommerceApplymentResult queryEcommerceApplyment(String outRequestNo) {
        String url = String.format(ECOMMERCE_APPLYMENT_QUERY_URL_TEMPLATE, UrlEncoder.urlEncode(outRequestNo));
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.GET)
                .url(url)
                .headers(headers)
                .build();
        try {
            HttpResponse<EcommerceApplymentResponse> response = httpClient.execute(httpRequest,
                    EcommerceApplymentResponse.class);
            return toApplymentResult(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("收付通进件查询失败：outRequestNo={}", outRequestNo, ex);
            throw new BusinessException(ResultCode.PAY_QUERY_FAILED, "收付通进件查询失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatMediaUploadResult uploadMedia(String fileName, byte[] content) {
        String normalizedName = normalizeMediaFileName(fileName);
        if (content == null || content.length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件内容为空");
        }
        if (content.length > MEDIA_MAX_BYTES) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片大小不能超过 5MB");
        }
        String sha256 = sha256Hex(content);
        String meta = "{\"filename\":\"" + escapeJson(normalizedName) + "\",\"sha256\":\"" + sha256 + "\"}";
        try {
            FileUploadResponse response =
                    fileUploadService.uploadImage(MERCHANT_MEDIA_UPLOAD_URL, meta, normalizedName, content);
            if (response == null || !StringUtils.hasText(response.getMediaId())) {
                throw new BusinessException(ResultCode.PAY_MEDIA_UPLOAD_FAILED, "微信媒体上传未返回 media_id");
            }
            return new WechatMediaUploadResult(response.getMediaId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("微信媒体文件上传失败：fileName={}", normalizedName, ex);
            throw new BusinessException(ResultCode.PAY_MEDIA_UPLOAD_FAILED, "微信媒体文件上传失败");
        }
    }

    private static String normalizeMediaFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件名不能为空");
        }
        String name = fileName.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名须包含 jpg/jpeg/png/bmp 后缀");
        }
        String suffix = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!MEDIA_IMAGE_SUFFIXES.contains(suffix)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 JPG/JPEG/PNG/BMP 图片");
        }
        // 微信示例以 JPG/BMP/PNG 为后缀；jpeg 保留原样亦可，统一小写
        return name.substring(0, dot) + "." + suffix;
    }

    private static String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "计算文件摘要失败");
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatRefundParseResult createEcommerceRefund(WechatEcommerceRefundCommand command) {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setSubMchid(command.subMchid());
        if (StringUtils.hasText(command.transactionId())) {
            request.setTransactionId(command.transactionId());
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
        RefundReqAmount amount = new RefundReqAmount();
        amount.setRefund((long) command.refundAmount());
        amount.setTotal((long) command.totalAmount());
        amount.setCurrency("CNY");
        request.setAmount(amount);
        try {
            Refund4Create resp = ecommerceRefundService.createRefund(request);
            Integer refund = null;
            if (resp.getAmount() != null && resp.getAmount().getRefund() != null) {
                refund = resp.getAmount().getRefund().intValue();
            }
            return new WechatRefundParseResult(
                    resp.getOutRefundNo(),
                    command.outTradeNo(),
                    resp.getRefundId(),
                    command.transactionId(),
                    refund,
                    // Refund4Create 不含 status；申请受理成功后按处理中落库，后续靠查单/回调收敛
                    PaymentRefundStatus.PROCESSING,
                    null);
        } catch (com.wechat.pay.java.core.exception.ServiceException ex) {
            if (WechatPayChannelErrorClassifier.isClearReject(ex)) {
                log.warn(
                        "收付通退款申请被明确拒绝：outRefundNo={}, errorCode={}",
                        command.outRefundNo(),
                        ex.getErrorCode(),
                        ex);
                throw new BusinessException(
                        ResultCode.WECHAT_PAY_REFUND_FAILED,
                        "收付通退款申请失败：" + ex.getErrorCode(),
                        ex);
            }
            log.warn(
                    "收付通退款申请结果不确定：outRefundNo={}, errorCode={}",
                    command.outRefundNo(),
                    ex.getErrorCode(),
                    ex);
            throw new BusinessException(ResultCode.PAY_REFUND_FAILED, "收付通退款调用异常，结果不确定", ex);
        } catch (RuntimeException ex) {
            log.warn("收付通退款申请异常（结果不确定）：outRefundNo={}", command.outRefundNo(), ex);
            throw new BusinessException(ResultCode.PAY_REFUND_FAILED, "收付通退款调用异常，结果不确定", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse createPayScoreServiceOrder(WechatPayScoreCreateCommand cmd) {
        PayScoreServiceOrderRequest body = new PayScoreServiceOrderRequest();
        body.setOutOrderNo(cmd.outOrderNo());
        body.setAppid(cmd.appId());
        body.setServiceId(cmd.serviceId());
        body.setServiceIntroduction(cmd.serviceIntroduction());
        body.setOpenid(cmd.openid());
        body.setNeedUserConfirm(cmd.needUserConfirm());
        if (StringUtils.hasText(cmd.notifyUrl())) {
            body.setNotifyUrl(cmd.notifyUrl());
        }
        if (StringUtils.hasText(cmd.attach())) {
            body.setAttach(cmd.attach());
        }
        PayScoreServiceOrderRequest.PayScoreRiskFund riskFund = new PayScoreServiceOrderRequest.PayScoreRiskFund();
        riskFund.setName(cmd.riskFundName());
        riskFund.setAmount(cmd.riskFundAmountCents());
        body.setRiskFund(riskFund);
        if (StringUtils.hasText(cmd.timeRangeStartTime()) || StringUtils.hasText(cmd.timeRangeEndTime())) {
            PayScoreServiceOrderRequest.PayScoreTimeRange timeRange =
                    new PayScoreServiceOrderRequest.PayScoreTimeRange();
            timeRange.setStartTime(cmd.timeRangeStartTime());
            timeRange.setEndTime(cmd.timeRangeEndTime());
            body.setTimeRange(timeRange);
        }
        if (StringUtils.hasText(cmd.locationName())) {
            PayScoreServiceOrderRequest.PayScoreLocation location =
                    new PayScoreServiceOrderRequest.PayScoreLocation();
            location.setName(cmd.locationName());
            body.setLocation(location);
        }
        try {
            HttpResponse<PayScoreServiceOrderResponse> response =
                    postJson(PAY_SCORE_SERVICE_ORDER_URL, body, PayScoreServiceOrderResponse.class);
            return requirePayScoreOrderResponse(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信支付分创建服务订单失败：outOrderNo={}", cmd.outOrderNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分创建服务订单失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse queryPayScoreServiceOrder(
            String appId, String serviceId, String outOrderNo) {
        String url = PAY_SCORE_SERVICE_ORDER_URL
                + "?service_id="
                + UrlEncoder.urlEncode(serviceId)
                + "&out_order_no="
                + UrlEncoder.urlEncode(outOrderNo)
                + "&appid="
                + UrlEncoder.urlEncode(appId);
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.GET)
                .url(url)
                .headers(headers)
                .build();
        try {
            HttpResponse<PayScoreServiceOrderResponse> response =
                    httpClient.execute(httpRequest, PayScoreServiceOrderResponse.class);
            return requirePayScoreOrderResponse(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信支付分查询服务订单失败：outOrderNo={}", outOrderNo, ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分查询服务订单失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse cancelPayScoreServiceOrder(WechatPayScoreCancelCommand cmd) {
        PayScoreCancelOrderRequest body = new PayScoreCancelOrderRequest();
        body.setAppid(cmd.appId());
        body.setServiceId(cmd.serviceId());
        body.setReason(cmd.reason());
        String url = String.format(
                PAY_SCORE_SERVICE_ORDER_CANCEL_URL_TEMPLATE, UrlEncoder.urlEncode(cmd.outOrderNo()));
        try {
            HttpResponse<PayScoreServiceOrderResponse> response =
                    postJson(url, body, PayScoreServiceOrderResponse.class);
            return requirePayScoreOrderResponse(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信支付分取消服务订单失败：outOrderNo={}", cmd.outOrderNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分取消服务订单失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScoreServiceOrderResponse completePayScoreServiceOrder(WechatPayScoreCompleteCommand cmd) {
        PayScoreCompleteOrderRequest body = new PayScoreCompleteOrderRequest();
        body.setAppid(cmd.appId());
        body.setServiceId(cmd.serviceId());
        body.setTotalAmount(cmd.totalAmountCents());
        body.setPostPayments(toPostPaymentItems(cmd.postPayments()));
        body.setPostDiscounts(toPostPaymentItems(cmd.postDiscounts()));
        if (StringUtils.hasText(cmd.timeRangeEndTime())) {
            PayScoreServiceOrderRequest.PayScoreTimeRange timeRange =
                    new PayScoreServiceOrderRequest.PayScoreTimeRange();
            timeRange.setEndTime(cmd.timeRangeEndTime());
            body.setTimeRange(timeRange);
        }
        if (StringUtils.hasText(cmd.completeTime())) {
            body.setCompleteTime(cmd.completeTime());
        }
        String url = String.format(
                PAY_SCORE_SERVICE_ORDER_COMPLETE_URL_TEMPLATE, UrlEncoder.urlEncode(cmd.outOrderNo()));
        try {
            HttpResponse<PayScoreServiceOrderResponse> response =
                    postJson(url, body, PayScoreServiceOrderResponse.class);
            return requirePayScoreOrderResponse(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信支付分完结服务订单失败：outOrderNo={}", cmd.outOrderNo(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分完结服务订单失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScorePermissionResponse createPayScorePermission(WechatPayScorePermissionCommand cmd) {
        PayScorePermissionRequest body = new PayScorePermissionRequest();
        body.setAppid(cmd.appId());
        body.setServiceId(cmd.serviceId());
        body.setAuthorizationCode(cmd.authorizationCode());
        if (StringUtils.hasText(cmd.notifyUrl())) {
            body.setNotifyUrl(cmd.notifyUrl());
        }
        try {
            HttpResponse<PayScorePermissionResponse> response =
                    postJson(PAY_SCORE_PERMISSIONS_URL, body, PayScorePermissionResponse.class);
            return requirePayScorePermissionResponse(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信支付分创建授权失败：serviceId={}", cmd.serviceId(), ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分创建授权失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PayScorePermissionResponse queryPayScorePermissionByOpenid(
            String appId, String serviceId, String openid) {
        String url = String.format(PAY_SCORE_PERMISSION_BY_OPENID_URL_TEMPLATE, UrlEncoder.urlEncode(openid))
                + "?appid="
                + UrlEncoder.urlEncode(appId)
                + "&service_id="
                + UrlEncoder.urlEncode(serviceId);
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.GET)
                .url(url)
                .headers(headers)
                .build();
        try {
            HttpResponse<PayScorePermissionResponse> response =
                    httpClient.execute(httpRequest, PayScorePermissionResponse.class);
            return requirePayScorePermissionResponse(response.getServiceResponse());
        } catch (RuntimeException ex) {
            log.warn("微信支付分查询授权失败：openid={}", openid, ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分查询授权失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminatePayScorePermissionByOpenid(
            String appId, String serviceId, String openid, String reason) {
        PayScorePermissionRequest body = new PayScorePermissionRequest();
        body.setAppid(appId);
        body.setServiceId(serviceId);
        body.setReason(reason);
        String url =
                String.format(PAY_SCORE_PERMISSION_TERMINATE_URL_TEMPLATE, UrlEncoder.urlEncode(openid));
        try {
            postJson(url, body, PayScorePermissionResponse.class);
        } catch (RuntimeException ex) {
            log.warn("微信支付分解除授权失败：openid={}", openid, ex);
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分解除授权失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WechatPayScoreParseResult parsePayScoreNotify(ChannelNotifyContext context) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SERIAL))
                .nonce(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.NONCE))
                .signature(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.SIGNATURE))
                .timestamp(WechatPayNotifyHeaderUtils.requireHeader(context, WechatPayNotifyHeaders.TIMESTAMP))
                .body(context.rawBody())
                .build();
        try {
            WechatPayScoreNotification notification =
                    notificationParser.parse(requestParam, WechatPayScoreNotification.class);
            return new WechatPayScoreParseResult(
                    notification.getOutOrderNo(),
                    notification.getOrderId(),
                    notification.getState(),
                    notification.getStateDescription(),
                    notification.getTotalAmount(),
                    notification.getOpenid(),
                    notification.getPackageInfo(),
                    notification.getEventType());
        } catch (RuntimeException ex) {
            log.warn("微信支付分回调验签或解密失败", ex);
            throw new BusinessException(ResultCode.PAY_SCORE_NOTIFY_PARSE_FAILED, "微信支付分回调验签或解密失败");
        }
    }

    private <T> HttpResponse<T> postJson(String url, Object body, Class<T> responseType) {
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.POST)
                .url(url)
                .headers(jsonHeaders())
                .body(new JsonRequestBody.Builder().body(GsonUtil.toJson(body)).build())
                .build();
        return httpClient.execute(httpRequest, responseType);
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("Accept", MediaType.APPLICATION_JSON.getValue());
        headers.addHeader("Content-Type", MediaType.APPLICATION_JSON.getValue());
        return headers;
    }

    private static WechatProfitSharingCreateResult toProfitSharingCreateResult(CreateOrderResponse resp) {
        String status = resp.getStatus() == null ? null : resp.getStatus().name();
        return new WechatProfitSharingCreateResult(
                resp.getSubMchid(),
                resp.getTransactionId(),
                resp.getOutOrderNo(),
                resp.getOrderId(),
                status);
    }

    private static WechatEcommerceApplymentResult toApplymentResult(EcommerceApplymentResponse resp) {
        if (resp == null) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "收付通进件响应为空");
        }
        return new WechatEcommerceApplymentResult(
                resp.getApplymentId(),
                resp.getOutRequestNo(),
                resp.getSubMchid(),
                resp.getApplymentState(),
                resp.getSignUrl());
    }

    private static WechatPayNotifyParseResult toCombineParseResult(CombineOrderNotification resp) {
        if (resp == null) {
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "收付通合单响应为空");
        }
        String combineOutTradeNo = resp.getCombineOutTradeNo();
        String transactionId = null;
        String tradeState = null;
        int amountTotal = 0;
        boolean hasAmount = false;
        if (resp.getSubOrders() != null) {
            for (CombineSubOrderResult sub : resp.getSubOrders()) {
                if (sub == null) {
                    continue;
                }
                if (!StringUtils.hasText(transactionId)) {
                    transactionId = sub.getTransactionId();
                }
                String state = sub.getTradeState();
                if (!StringUtils.hasText(tradeState)) {
                    tradeState = state;
                } else if (!"SUCCESS".equals(state)) {
                    tradeState = state;
                }
                if (sub.getAmount() != null) {
                    Integer totalAmount = sub.getAmount().resolveTotal();
                    if (totalAmount != null) {
                        amountTotal += totalAmount;
                        hasAmount = true;
                    }
                }
            }
        }
        if (!StringUtils.hasText(tradeState)) {
            tradeState = resp.getTradeState();
        }
        return new WechatPayNotifyParseResult(
                combineOutTradeNo, transactionId, tradeState, hasAmount ? amountTotal : null);
    }

    private static WechatTransferParseResult toTransferParseResult(TransferBillResponse response) {
        if (response == null) {
            throw new BusinessException(ResultCode.WECHAT_PAY_TRANSFER_FAILED, "微信商家转账响应为空");
        }
        return new WechatTransferParseResult(
                response.getOutBillNo(),
                response.getTransferBillNo(),
                response.getTransferAmount(),
                response.getState(),
                response.getPackageInfo(),
                response.getFailReason());
    }

    private static PayScoreServiceOrderResponse requirePayScoreOrderResponse(
            PayScoreServiceOrderResponse response) {
        if (response == null) {
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分服务订单响应为空");
        }
        return response;
    }

    private static PayScorePermissionResponse requirePayScorePermissionResponse(
            PayScorePermissionResponse response) {
        if (response == null) {
            throw new BusinessException(ResultCode.WECHAT_PAY_SCORE_FAILED, "微信支付分授权响应为空");
        }
        return response;
    }

    private static List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> toPostPaymentItems(
            List<PayScorePostPayment> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> result = new ArrayList<>();
        for (PayScorePostPayment item : items) {
            if (item == null) {
                continue;
            }
            PayScoreServiceOrderRequest.PayScorePostPaymentItem mapped =
                    new PayScoreServiceOrderRequest.PayScorePostPaymentItem();
            mapped.setName(item.getName());
            mapped.setAmount(item.getAmountCents());
            mapped.setDescription(item.getDescription());
            mapped.setCount(item.getCount());
            result.add(mapped);
        }
        return result.isEmpty() ? null : result;
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

    private static WechatRefundParseResult toEcommerceRefundParseResult(
            com.wechat.pay.java.service.ecommercerefund.model.Refund refund) {
        Integer amountRefund = null;
        if (refund.getAmount() != null && refund.getAmount().getRefund() != null) {
            amountRefund = refund.getAmount().getRefund().intValue();
        }
        return new WechatRefundParseResult(
                refund.getOutRefundNo(),
                refund.getOutTradeNo(),
                refund.getRefundId(),
                refund.getTransactionId(),
                amountRefund,
                refund.getStatus(),
                refund.getUserReceivedAccount());
    }

    private static WechatRefundParseResult toEcommerceRefundNotifyParseResult(
            WechatEcommerceRefundNotification notification) {
        Integer amountRefund = null;
        if (notification.getAmount() != null && notification.getAmount().getRefund() != null) {
            amountRefund = notification.getAmount().getRefund();
        }
        return new WechatRefundParseResult(
                notification.getOutRefundNo(),
                notification.getOutTradeNo(),
                notification.getRefundId(),
                notification.getTransactionId(),
                amountRefund,
                notification.getRefundStatus(),
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
        if (properties.usePublicKeyVerifier()) {
            return new RSAPublicKeyConfig.Builder()
                    .merchantId(properties.getMchId())
                    .privateKeyFromPath(properties.getPrivateKeyPath())
                    .merchantSerialNumber(properties.getMerchantSerialNo())
                    .publicKeyFromPath(properties.getPublicKeyPath())
                    .publicKeyId(properties.getPublicKeyId())
                    .apiV3Key(properties.getApiV3Key())
                    .build();
        }
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
