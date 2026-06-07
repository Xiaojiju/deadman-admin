package com.mtfm.deadman.plugin.storage.local.config;

import com.mtfm.deadman.plugin.storage.local.provider.LocalFileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将本地存储目录映射为静态资源，支持通过 URL 直接访问文件。
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.storage-local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalStorageWebMvcConfig implements WebMvcConfigurer {

    private final LocalFileStorageProvider localFileStorageProvider;
    private final LocalStoragePluginProperties properties;

    /**
     * 注册静态资源映射：{@code publicUrlPrefix/**} → 本地存储目录。
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = normalizePrefix(properties.getPublicUrlPrefix());
        String location = localFileStorageProvider.resolveBasePath().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(prefix + "/**").addResourceLocations(location);
    }

    private static String normalizePrefix(String prefix) {
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix.endsWith("/")) {
            return prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }
}
