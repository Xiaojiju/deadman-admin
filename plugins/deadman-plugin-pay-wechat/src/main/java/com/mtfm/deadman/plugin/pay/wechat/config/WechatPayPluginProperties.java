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

    /** 是否使用 Mock 网关（无真实商户号或显式开启时生效） */
    private boolean mockEnabled = true;

    /** 微信支付商户号 */
    private String mchId;

    /** APIv3 密钥 */
    private String apiV3Key;

    /** 商户 API 证书序列号 */
    private String merchantSerialNo;

    /** 商户 API 私钥 PEM 文件路径 */
    private String privateKeyPath;

    /** 各支付 Provider 独立绑定配置，键为 Provider 标识（如 wechat-jsapi） */
    private Map<String, WechatPayProviderBindingProperties> providers = new LinkedHashMap<>();

    /**
     * 是否应使用 Mock 网关。
     *
     * @return 是否 Mock
     */
    public boolean shouldUseMock() {
        if (mockEnabled) {
            return true;
        }
        return !StringUtils.hasText(mchId)
                || !StringUtils.hasText(apiV3Key)
                || !StringUtils.hasText(merchantSerialNo)
                || !StringUtils.hasText(privateKeyPath);
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
        return binding;
    }

    /**
     * 列出所有已启用 Provider 的回调 endpoint。
     *
     * @return endpoint 路径列表
     */
    public List<String> enabledNotifyEndpoints() {
        List<String> endpoints = new ArrayList<>();
        for (Map.Entry<String, WechatPayProviderBindingProperties> entry : providers.entrySet()) {
            WechatPayProviderBindingProperties binding = entry.getValue();
            if (binding != null && binding.isEnabled()) {
                endpoints.add(normalizeEndpoint(binding.getNotifyEndpoint(), entry.getKey()));
            }
        }
        return endpoints;
    }

    /**
     * 解析 Provider 默认回调 endpoint。
     *
     * @param providerId Provider 标识
     * @return endpoint 路径
     */
    public static String defaultNotifyEndpoint(String providerId) {
        return switch (providerId) {
            case "wechat-jsapi" -> "/client/api/pay/wechat/jsapi/notify";
            case "wechat-native" -> "/client/api/pay/wechat/native/notify";
            default -> "/client/api/pay/wechat/" + providerId + "/notify";
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
        if (!resolved.startsWith("/")) {
            resolved = "/" + resolved;
        }
        if (resolved.endsWith("/")) {
            return resolved.substring(0, resolved.length() - 1);
        }
        return resolved;
    }
}
