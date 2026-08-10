package com.mtfm.deadman.plugin.pay.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * 退款 Provider 管理器，聚合所有已注册的退款 Provider 实现。
 */
@Slf4j
@Component
public class RefundProviderManager {

    private final Map<String, RefundProvider> providers;

    /**
     * 构造退款 Provider 管理器。
     *
     * @param providerList 所有退款 Provider Bean
     */
    public RefundProviderManager(List<RefundProvider> providerList) {
        Map<String, RefundProvider> registry = new LinkedHashMap<>();
        for (RefundProvider provider : providerList) {
            String providerId = provider.providerId();
            if (registry.containsKey(providerId)) {
                log.warn("退款 Provider 重复注册，后者覆盖前者：{}", providerId);
            }
            registry.put(providerId, provider);
        }
        this.providers = Map.copyOf(registry);
        log.info("退款 Provider 注册完成，共 {} 个：{}", providers.size(), providers.keySet());
    }

    /**
     * 按标识获取退款 Provider。
     *
     * @param providerId 提供商标识
     * @return Provider 实例
     */
    public RefundProvider require(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            throw new BusinessException(ResultCode.PAY_REFUND_PROVIDER_NOT_FOUND, "退款 Provider 标识为空");
        }
        RefundProvider provider = providers.get(providerId);
        if (provider == null) {
            throw new BusinessException(ResultCode.PAY_REFUND_PROVIDER_NOT_FOUND, "退款 Provider 不存在：" + providerId);
        }
        return provider;
    }

    /**
     * 已注册的退款 Provider 标识列表。
     *
     * @return 提供商标识列表
     */
    public List<String> listProviderIds() {
        return List.copyOf(providers.keySet());
    }
}
