package com.mtfm.deadman.plugin.pay.wechat.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import lombok.Data;

/**
 * 微信支付插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.pay-wechat")
public class WechatPayPluginProperties {

    /** 是否启用微信支付插件（商户级共享配置） */
    private boolean enabled = false;

    /** 是否使用 Mock 网关（仅显式开启时生效；生产务必为 false） */
    private boolean mockEnabled = false;

    /**
     * 普通直连商户号（兼容旧配置；未填 {@link #ordinary} 时作为 ordinary 回退）。
     */
    private String mchId;

    /**
     * 合作伙伴/平台商户号（兼容旧配置；未填 {@link #partner} 时用同一套密钥替换商户号）。
     */
    private String partnerMchid;

    /** APIv3 密钥（兼容旧配置，回退到 ordinary） */
    private String apiV3Key;

    /** 商户 API 证书序列号（兼容旧配置，回退到 ordinary） */
    private String merchantSerialNo;

    /** 商户 API 私钥 PEM 文件路径（兼容旧配置，回退到 ordinary） */
    private String privateKeyPath;

    /**
     * 微信支付公钥 PEM 文件路径（兼容旧配置，回退到 ordinary）。
     * 配置后走公钥模式 {@code RSAPublicKeyConfig}；均未配置时回退平台证书自动更新。
     */
    private String publicKeyPath;

    /** 微信支付公钥 ID（兼容旧配置，回退到 ordinary），形如 {@code PUB_KEY_ID_...} */
    private String publicKeyId;

    /**
     * 普通直连商户凭证。会员 JSAPI、直连退款、商家转账、支付分使用该账户。
     * 未填字段回退顶层 {@code mch-id} / {@code api-v3-key} 等旧配置。
     */
    private WechatPayAccountProperties ordinary = new WechatPayAccountProperties();

    /**
     * 合作伙伴/受理机构凭证。备件收付通合单、电商退款、分账、进件使用该账户。
     * 未配置时回退普通商户；仅配置了顶层 {@code partner-mchid} 时用 ordinary 密钥替换商户号。
     */
    private WechatPayAccountProperties partner = new WechatPayAccountProperties();

    /** 收付通相关扩展配置 */
    private Ecommerce ecommerce = new Ecommerce();

    /** 各支付 Provider 独立绑定配置，键为 Provider 标识（如 wechat-jsapi） */
    private Map<String, WechatPayProviderBindingProperties> providers = new LinkedHashMap<>();

    /**
     * 解析普通直连商户号（会员 JSAPI）；未填嵌套 ordinary 时回退顶层 {@link #mchId}。
     *
     * @return 普通商户号
     */
    public String getMchId() {
        return resolveOrdinaryAccount().getMchId();
    }

    /**
     * 获取平台/服务商商户号；未配置时回退普通商户号。
     *
     * @return 平台商户号
     */
    public String getPartnerMchid() {
        return resolvePartnerMchid();
    }

    /**
     * 解析合作伙伴/平台商户号（备件收付通合单 {@code combine_mchid}）。
     *
     * @return 平台商户号
     */
    public String resolvePartnerMchid() {
        return resolvePartnerAccount().resolvePartnerMchid();
    }

    /**
     * 解析普通直连商户凭证（会员支付）。
     *
     * @return ordinary 账户，空字段已用顶层旧配置回填
     */
    public WechatPayAccountProperties resolveOrdinaryAccount() {
        WechatPayAccountProperties resolved =
                ordinary == null ? new WechatPayAccountProperties() : ordinary.copy();
        resolved.fillBlanksFrom(rootAsAccount());
        return resolved;
    }

    /**
     * 解析合作伙伴凭证（备件收付通）。
     * <p>
     * 已显式配置 {@link #partner} 时不借用 ordinary 密钥；未配置时回退 ordinary，
     * 若仅配置了顶层 {@link #partnerMchid} 则用 ordinary 密钥替换商户号。
     *
     * @return partner 账户
     */
    public WechatPayAccountProperties resolvePartnerAccount() {
        WechatPayAccountProperties resolved =
                partner == null ? new WechatPayAccountProperties() : partner.copy();
        if (resolved.hasAnyCredential()) {
            return resolved;
        }
        WechatPayAccountProperties fallback = resolveOrdinaryAccount().copy();
        if (StringUtils.hasText(this.partnerMchid)) {
            fallback.setMchId(this.partnerMchid.trim());
            fallback.setPartnerMchid(this.partnerMchid.trim());
        }
        return fallback;
    }

    /**
     * 是否应使用 Mock 网关。
     * <p>
     * 仅 {@code mock-enabled=true} 时走 Mock；凭证缺失时不再静默降级，避免生产误用无验签网关。
     *
     * @return 是否 Mock
     */
    public boolean shouldUseMock() {
        return mockEnabled;
    }

    /**
     * 校验真实网关所需商户凭证是否齐全。
     *
     * @throws IllegalStateException 凭证缺失
     */
    public void requireRealGatewayCredentials() {
        try {
            resolveOrdinaryAccount().requireComplete("ordinary");
            resolvePartnerAccount().requireComplete("partner");
        } catch (IllegalStateException ex) {
            throw new IllegalStateException(
                    ex.getMessage() + "，请补齐凭证或显式开启 deadman.plugin.pay-wechat.mock-enabled=true",
                    ex);
        }
    }

    /**
     * 普通商户是否使用微信支付公钥验签（相对平台证书自动更新）。
     *
     * @return 公钥路径与公钥 ID 均已配置则为 true
     */
    public boolean usePublicKeyVerifier() {
        return resolveOrdinaryAccount().usePublicKeyVerifier();
    }

    /**
     * 将顶层旧配置映射为一套账户凭证（仅内部回退使用）。
     *
     * @return 顶层字段组成的账户
     */
    private WechatPayAccountProperties rootAsAccount() {
        WechatPayAccountProperties root = new WechatPayAccountProperties();
        root.setMchId(this.mchId);
        root.setPartnerMchid(this.partnerMchid);
        root.setApiV3Key(this.apiV3Key);
        root.setMerchantSerialNo(this.merchantSerialNo);
        root.setPrivateKeyPath(this.privateKeyPath);
        root.setPublicKeyPath(this.publicKeyPath);
        root.setPublicKeyId(this.publicKeyId);
        return root;
    }

    /**
     * 获取指定 Provider 的绑定配置。
     *
     * @param providerId Provider 标识
     * @return 绑定配置，不存在时返回 disabled 默认实例
     */
    public WechatPayProviderBindingProperties providerBinding(String providerId) {
        WechatPayProviderBindingProperties binding = providers.get(providerId);
        if (binding == null) {
            WechatPayProviderBindingProperties defaults = new WechatPayProviderBindingProperties();
            defaults.setNotifyEndpoint(defaultNotifyEndpoint(providerId));
            return defaults;
        }
        if (!StringUtils.hasText(binding.getNotifyEndpoint())) {
            binding.setNotifyEndpoint(defaultNotifyEndpoint(providerId));
        }
        if (!StringUtils.hasText(binding.getRefundNotifyEndpoint())) {
            binding.setRefundNotifyEndpoint(defaultRefundNotifyEndpoint(providerId));
        }
        if (!StringUtils.hasText(binding.getTransferNotifyEndpoint())) {
            binding.setTransferNotifyEndpoint(defaultTransferNotifyEndpoint(providerId));
        }
        if (!StringUtils.hasText(binding.getPayScoreNotifyEndpoint())) {
            binding.setPayScoreNotifyEndpoint(defaultPayScoreNotifyEndpoint(providerId));
        }
        return binding;
    }

    /**
     * 列出所有已启用 Provider 的支付/退款/转账/支付分回调 endpoint。
     *
     * @return endpoint 路径列表
     */
    public List<String> enabledNotifyEndpoints() {
        List<String> endpoints = new ArrayList<>();
        for (Map.Entry<String, WechatPayProviderBindingProperties> entry : providers.entrySet()) {
            WechatPayProviderBindingProperties binding = entry.getValue();
            if (binding != null && binding.isEnabled()) {
                endpoints.add(normalizeEndpoint(binding.getNotifyEndpoint(), entry.getKey()));
                endpoints.add(normalizeRefundEndpoint(binding.getRefundNotifyEndpoint(), entry.getKey()));
                endpoints.add(normalizeTransferEndpoint(binding.getTransferNotifyEndpoint(), entry.getKey()));
                endpoints.add(normalizePayScoreEndpoint(binding.getPayScoreNotifyEndpoint(), entry.getKey()));
            }
        }
        return endpoints;
    }

    /**
     * 解析 Provider 默认支付回调 endpoint。
     *
     * @param providerId Provider 标识
     * @return endpoint 路径
     */
    public static String defaultNotifyEndpoint(String providerId) {
        return switch (providerId) {
            case "wechat-jsapi" -> "/client/api/pay/wechat/jsapi/notify";
            case "wechat-native" -> "/client/api/pay/wechat/native/notify";
            case "wechat-ecommerce-combine-jsapi" ->
                    "/client/api/pay/wechat/ecommerce-combine-jsapi/notify";
            default -> "/client/api/pay/wechat/" + providerId + "/notify";
        };
    }

    /**
     * 解析 Provider 默认退款回调 endpoint。
     *
     * @param providerId Provider 标识
     * @return endpoint 路径
     */
    public static String defaultRefundNotifyEndpoint(String providerId) {
        return switch (providerId) {
            case "wechat-jsapi" -> "/client/api/pay/wechat/jsapi/refund/notify";
            case "wechat-native" -> "/client/api/pay/wechat/native/refund/notify";
            case "wechat-ecommerce-combine-jsapi" ->
                    "/client/api/pay/wechat/ecommerce-combine-jsapi/refund/notify";
            default -> "/client/api/pay/wechat/" + providerId + "/refund/notify";
        };
    }

    /**
     * 解析 Provider 默认商家转账回调 endpoint。
     *
     * @param providerId Provider 标识
     * @return endpoint 路径
     */
    public static String defaultTransferNotifyEndpoint(String providerId) {
        return switch (providerId) {
            case "wechat-jsapi" -> "/client/api/pay/wechat/jsapi/transfer/notify";
            case "wechat-native" -> "/client/api/pay/wechat/native/transfer/notify";
            default -> "/client/api/pay/wechat/" + providerId + "/transfer/notify";
        };
    }

    /**
     * 解析 Provider 默认支付分回调 endpoint。
     *
     * @param providerId Provider 标识
     * @return endpoint 路径
     */
    public static String defaultPayScoreNotifyEndpoint(String providerId) {
        return switch (providerId) {
            case "wechat-payscore" -> "/client/api/pay/wechat/payscore/notify";
            default -> "/client/api/pay/wechat/" + providerId + "/payscore/notify";
        };
    }

    /**
     * 规范化 endpoint 路径。
     *
     * @param endpoint   原始路径
     * @param providerId Provider 标识（用于回退默认值）
     * @return 规范化路径
     */
    public static String normalizeEndpoint(String endpoint, String providerId) {
        String resolved = StringUtils.hasText(endpoint) ? endpoint.trim() : defaultNotifyEndpoint(providerId);
        return normalizePath(resolved);
    }

    /**
     * 规范化退款回调 endpoint 路径。
     *
     * @param endpoint   原始路径
     * @param providerId Provider 标识
     * @return 规范化路径
     */
    public static String normalizeRefundEndpoint(String endpoint, String providerId) {
        String resolved =
                StringUtils.hasText(endpoint) ? endpoint.trim() : defaultRefundNotifyEndpoint(providerId);
        return normalizePath(resolved);
    }

    /**
     * 规范化商家转账回调 endpoint 路径。
     *
     * @param endpoint   原始路径
     * @param providerId Provider 标识
     * @return 规范化路径
     */
    public static String normalizeTransferEndpoint(String endpoint, String providerId) {
        String resolved =
                StringUtils.hasText(endpoint) ? endpoint.trim() : defaultTransferNotifyEndpoint(providerId);
        return normalizePath(resolved);
    }

    /**
     * 规范化支付分回调 endpoint 路径。
     *
     * @param endpoint   原始路径
     * @param providerId Provider 标识
     * @return 规范化路径
     */
    public static String normalizePayScoreEndpoint(String endpoint, String providerId) {
        String resolved =
                StringUtils.hasText(endpoint) ? endpoint.trim() : defaultPayScoreNotifyEndpoint(providerId);
        return normalizePath(resolved);
    }

    private static String normalizePath(String resolved) {
        if (!resolved.startsWith("/")) {
            resolved = "/" + resolved;
        }
        if (resolved.endsWith("/")) {
            return resolved.substring(0, resolved.length() - 1);
        }
        return resolved;
    }

    /**
     * 收付通扩展配置。
     */
    @Data
    public static class Ecommerce {

        /** 分账结果回调 URL */
        private String profitSharingNotifyUrl;

        /** 二级商户进件结果回调 URL */
        private String applymentNotifyUrl;
    }
}
