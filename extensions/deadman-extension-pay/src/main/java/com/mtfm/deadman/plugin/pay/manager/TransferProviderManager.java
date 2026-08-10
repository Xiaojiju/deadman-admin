package com.mtfm.deadman.plugin.pay.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * 商家转账 Provider 管理器。
 */
@Slf4j
@Component
public class TransferProviderManager {

    private final Map<String, TransferProvider> providers;
    private final String defaultProviderId;

    /**
     * 构造转账 Provider 管理器。
     *
     * @param providerList  所有转账 Provider Bean
     * @param payProperties 支付插件配置
     */
    public TransferProviderManager(List<TransferProvider> providerList, PayPluginProperties payProperties) {
        Map<String, TransferProvider> registry = new LinkedHashMap<>();
        for (TransferProvider provider : providerList) {
            String providerId = provider.providerId();
            if (registry.containsKey(providerId)) {
                log.warn("转账 Provider 重复注册，后者覆盖前者：{}", providerId);
            }
            registry.put(providerId, provider);
        }
        this.providers = Map.copyOf(registry);
        this.defaultProviderId = payProperties.getDefaultProvider();
        log.info(
                "转账 Provider 注册完成，共 {} 个：{}，默认：{}",
                providers.size(),
                providers.keySet(),
                defaultProviderId);
    }

    /**
     * 按标识获取转账 Provider；空标识时使用默认支付 Provider。
     *
     * @param providerId 提供商标识
     * @return Provider 实例
     */
    public TransferProvider require(String providerId) {
        String resolved = StringUtils.hasText(providerId) ? providerId.trim() : defaultProviderId;
        TransferProvider provider = providers.get(resolved);
        if (provider == null) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_PROVIDER_NOT_FOUND, "转账 Provider 不存在：" + resolved);
        }
        return provider;
    }

    /**
     * 已注册的转账 Provider 标识列表。
     *
     * @return 提供商标识列表
     */
    public List<String> listProviderIds() {
        return List.copyOf(providers.keySet());
    }
}
