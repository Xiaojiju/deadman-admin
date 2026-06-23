package com.mtfm.deadman.plugin.pay.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.spi.PaymentProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * 支付 Provider 管理器，聚合所有已注册的 Provider 实现。
 */
@Slf4j
@Component
public class PaymentProviderManager {

    private final Map<String, PaymentProvider> providers;
    private final String defaultProviderId;

    /**
     * 构造 Provider 管理器。
     *
     * @param providerList  所有 Provider Bean
     * @param payProperties 支付插件配置
     */
    public PaymentProviderManager(List<PaymentProvider> providerList, PayPluginProperties payProperties) {
        Map<String, PaymentProvider> registry = new LinkedHashMap<>();
        for (PaymentProvider provider : providerList) {
            String providerId = provider.providerId();
            if (registry.containsKey(providerId)) {
                log.warn("支付 Provider 重复注册，后者覆盖前者：{}", providerId);
            }
            registry.put(providerId, provider);
        }
        this.providers = Map.copyOf(registry);
        this.defaultProviderId = payProperties.getDefaultProvider();
        log.info("支付 Provider 注册完成，共 {} 个：{}，默认：{}", providers.size(), providers.keySet(), defaultProviderId);
    }

    /**
     * 获取默认 Provider。
     *
     * @return Provider 实例
     */
    public PaymentProvider requireDefault() {
        return require(defaultProviderId);
    }

    /**
     * 按标识获取 Provider。
     *
     * @param providerId 提供商标识，为空时使用默认
     * @return Provider 实例
     */
    public PaymentProvider require(String providerId) {
        String resolved = providerId == null || providerId.isBlank() ? defaultProviderId : providerId;
        PaymentProvider provider = providers.get(resolved);
        if (provider == null) {
            throw new BusinessException(ResultCode.PAY_PROVIDER_NOT_FOUND, "支付 Provider 不存在：" + resolved);
        }
        return provider;
    }

    /**
     * 已注册的 Provider 标识列表。
     *
     * @return 提供商标识列表
     */
    public List<String> listProviderIds() {
        return List.copyOf(providers.keySet());
    }
}
