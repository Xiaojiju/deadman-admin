package com.mtfm.deadman.plugin.pay.wechat.client;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayNotifyHeaders;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayNotifyHeaderUtils;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信支付 APIv3 真实网关实现。
 */
@Slf4j
public class WechatPayApiGatewayImpl implements WechatPayApiGateway {

    private final WechatPayPluginProperties properties;
    private final Config config;
    private final JsapiService jsapiService;
    private final NotificationParser notificationParser;

    /**
     * 构造真实微信支付网关。
     *
     * @param properties 插件配置
     */
    public WechatPayApiGatewayImpl(WechatPayPluginProperties properties) {
        this.properties = properties;
        this.config = buildConfig(properties);
        this.jsapiService = new JsapiService.Builder().config(config).build();
        this.notificationParser = new NotificationParser((RSAAutoCertificateConfig) config);
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
    public WechatPayNotifyParseResult parseNotify(PaymentNotifyContext context) {
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

    private static WechatPayNotifyParseResult toParseResult(Transaction transaction) {
        return new WechatPayNotifyParseResult(
                transaction.getOutTradeNo(), transaction.getTransactionId(), transaction.getTradeState().name());
    }

    private WechatPayRequestPaymentParams signRequestPayment(String appId, String prepayId) {
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String packageValue = "prepay_id=" + prepayId;
        String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageValue + "\n";
        try {
            PrivateKey privateKey = loadPrivateKey(properties.getPrivateKeyPath());
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
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
