package com.mtfm.deadman.plugin.logistics.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.logistics.spi.LogisticsCapabilityProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * 物流领域 Provider 注册表辅助类。
 *
 * @param <T> 领域 Provider 类型
 */
@Slf4j
final class LogisticsCapabilityRegistry<T extends LogisticsCapabilityProvider> {

    private final Map<String, T> providers;
    private final String capabilityName;

    LogisticsCapabilityRegistry(List<T> providerList, String capabilityName) {
        this.capabilityName = capabilityName;
        Map<String, T> registry = new LinkedHashMap<>();
        for (T provider : providerList) {
            String providerId = provider.providerId();
            if (registry.containsKey(providerId)) {
                log.warn("物流 {} Provider 重复注册，后者覆盖前者：{}", capabilityName, providerId);
            }
            registry.put(providerId, provider);
        }
        this.providers = Map.copyOf(registry);
        log.info("物流 {} Provider 注册完成，共 {} 个：{}", capabilityName, providers.size(), providers.keySet());
    }

    /**
     * 按标识获取 Provider。
     *
     * @param providerId       提供商标识
     * @param defaultProviderId 默认提供商标识
     * @return Provider 实例
     */
    T require(String providerId, String defaultProviderId) {
        String resolved = providerId == null || providerId.isBlank() ? defaultProviderId : providerId;
        T provider = providers.get(resolved);
        if (provider == null) {
            throw new BusinessException(
                    ResultCode.LOGISTICS_PROVIDER_NOT_FOUND, capabilityName + " Provider 不存在：" + resolved);
        }
        return provider;
    }

    /**
     * 已注册的 Provider 标识列表。
     *
     * @return 提供商标识列表
     */
    List<String> listProviderIds() {
        return List.copyOf(providers.keySet());
    }
}
