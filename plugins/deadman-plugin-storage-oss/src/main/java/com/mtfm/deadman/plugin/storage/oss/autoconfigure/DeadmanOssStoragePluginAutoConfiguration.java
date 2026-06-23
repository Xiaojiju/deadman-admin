package com.mtfm.deadman.plugin.storage.oss.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.plugin.storage.oss.config.OssStoragePluginProperties;

/**
 * 阿里云 OSS 存储插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(OssStoragePluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.storage-oss", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.storage.oss")
public class DeadmanOssStoragePluginAutoConfiguration {
}
