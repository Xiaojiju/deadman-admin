package com.mtfm.deadman.plugin.pay.wechat.client;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayAccountProperties;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.ecommerceprofitsharing.EcommerceProfitSharingService;
import com.wechat.pay.java.service.ecommercerefund.EcommerceRefundService;
import com.wechat.pay.java.service.file.FileUploadService;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.refund.RefundService;

/**
 * 单套微信支付商户运行时（证书、验签、HTTP、官方 SDK Service）。
 */
final class WechatPayMerchantClient {

    /** 该运行时绑定的商户凭证 */
    private final WechatPayAccountProperties account;
    /** 签名 HTTP 客户端 */
    private final HttpClient httpClient;
    /** 支付/退款回调验签解析器 */
    private final NotificationParser notificationParser;
    /** 商户 API 私钥（调起支付签名） */
    private final PrivateKey merchantPrivateKey;
    /** 直连 JSAPI 下单/查单 */
    private final JsapiService jsapiService;
    /** 直连退款 */
    private final RefundService refundService;
    /** 收付通分账 */
    private final EcommerceProfitSharingService ecommerceProfitSharingService;
    /** 收付通退款 */
    private final EcommerceRefundService ecommerceRefundService;
    /** 进件媒体上传 */
    private final FileUploadService fileUploadService;

    private WechatPayMerchantClient(
            WechatPayAccountProperties account,
            HttpClient httpClient,
            NotificationParser notificationParser,
            PrivateKey merchantPrivateKey,
            JsapiService jsapiService,
            RefundService refundService,
            EcommerceProfitSharingService ecommerceProfitSharingService,
            EcommerceRefundService ecommerceRefundService,
            FileUploadService fileUploadService) {
        this.account = account;
        this.httpClient = httpClient;
        this.notificationParser = notificationParser;
        this.merchantPrivateKey = merchantPrivateKey;
        this.jsapiService = jsapiService;
        this.refundService = refundService;
        this.ecommerceProfitSharingService = ecommerceProfitSharingService;
        this.ecommerceRefundService = ecommerceRefundService;
        this.fileUploadService = fileUploadService;
    }

    /**
     * 按一套商户凭证创建运行时。
     *
     * @param account 商户凭证
     * @return 运行时
     */
    static WechatPayMerchantClient create(WechatPayAccountProperties account) {
        Config config = buildConfig(account);
        HttpClient httpClient = new DefaultHttpClientBuilder().config(config).build();
        PrivateKey privateKey;
        try {
            privateKey = loadPrivateKey(account.getPrivateKeyPath());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.WECHAT_PAY_CONFIG_INVALID, "加载商户私钥失败");
        }
        return new WechatPayMerchantClient(
                account,
                httpClient,
                new NotificationParser((NotificationConfig) config),
                privateKey,
                new JsapiService.Builder().config(config).build(),
                new RefundService.Builder().config(config).build(),
                new EcommerceProfitSharingService.Builder().config(config).build(),
                new EcommerceRefundService.Builder().config(config).build(),
                new FileUploadService.Builder().httpClient(httpClient).build());
    }

    WechatPayAccountProperties account() {
        return account;
    }

    HttpClient httpClient() {
        return httpClient;
    }

    NotificationParser notificationParser() {
        return notificationParser;
    }

    PrivateKey merchantPrivateKey() {
        return merchantPrivateKey;
    }

    JsapiService jsapiService() {
        return jsapiService;
    }

    RefundService refundService() {
        return refundService;
    }

    EcommerceProfitSharingService ecommerceProfitSharingService() {
        return ecommerceProfitSharingService;
    }

    EcommerceRefundService ecommerceRefundService() {
        return ecommerceRefundService;
    }

    FileUploadService fileUploadService() {
        return fileUploadService;
    }

    private static Config buildConfig(WechatPayAccountProperties account) {
        if (account.usePublicKeyVerifier()) {
            return new RSAPublicKeyConfig.Builder()
                    .merchantId(account.getMchId())
                    .privateKeyFromPath(account.getPrivateKeyPath())
                    .merchantSerialNumber(account.getMerchantSerialNo())
                    .publicKeyFromPath(account.getPublicKeyPath())
                    .publicKeyId(account.getPublicKeyId())
                    .apiV3Key(account.getApiV3Key())
                    .build();
        }
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(account.getMchId())
                .privateKeyFromPath(account.getPrivateKeyPath())
                .merchantSerialNumber(account.getMerchantSerialNo())
                .apiV3Key(account.getApiV3Key())
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
