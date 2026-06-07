package com.mtfm.deadman.plugin.file.manager;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.file.config.FilePluginProperties;
import com.mtfm.deadman.plugin.file.spi.FileStorageProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文件存储 Provider 管理器，聚合所有已注册的 Provider 实现。
 */
@Slf4j
@Component
public class FileStorageProviderManager {

    private final Map<String, FileStorageProvider> providers;
    private final String defaultProviderId;

    /**
     * 构造 Provider 管理器。
     *
     * @param providerList   所有 Provider Bean
     * @param fileProperties 文件插件配置
     */
    public FileStorageProviderManager(List<FileStorageProvider> providerList, FilePluginProperties fileProperties) {
        Map<String, FileStorageProvider> registry = new LinkedHashMap<>();
        for (FileStorageProvider provider : providerList) {
            String providerId = provider.providerId();
            if (registry.containsKey(providerId)) {
                log.warn("文件存储 Provider 重复注册，后者覆盖前者：{}", providerId);
            }
            registry.put(providerId, provider);
        }
        this.providers = Map.copyOf(registry);
        this.defaultProviderId = fileProperties.getDefaultProvider();
        log.info("文件存储 Provider 注册完成，共 {} 个：{}，默认：{}", providers.size(), providers.keySet(), defaultProviderId);
    }

    /**
     * 获取默认 Provider。
     *
     * @return Provider 实例
     */
    public FileStorageProvider requireDefault() {
        return require(defaultProviderId);
    }

    /**
     * 按标识获取 Provider。
     *
     * @param providerId 提供商标识，为空时使用默认
     * @return Provider 实例
     */
    public FileStorageProvider require(String providerId) {
        String resolved = providerId == null || providerId.isBlank() ? defaultProviderId : providerId;
        FileStorageProvider provider = providers.get(resolved);
        if (provider == null) {
            throw new BusinessException(ResultCode.FILE_PROVIDER_NOT_FOUND, "文件存储 Provider 不存在：" + resolved);
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
