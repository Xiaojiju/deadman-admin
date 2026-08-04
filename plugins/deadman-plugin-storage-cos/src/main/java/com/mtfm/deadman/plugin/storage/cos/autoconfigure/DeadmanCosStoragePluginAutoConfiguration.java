package com.mtfm.deadman.plugin.storage.cos.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.plugin.storage.cos.config.CosStoragePluginProperties;

/**
 * 腾讯云 COS 存储插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(CosStoragePluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.storage-cos", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.storage.cos")
public class DeadmanCosStoragePluginAutoConfiguration {
}
