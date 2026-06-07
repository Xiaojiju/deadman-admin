package com.mtfm.deadman.plugin.storage.local.autoconfigure;

import com.mtfm.deadman.plugin.storage.local.config.LocalStoragePluginProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 本地磁盘存储插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(LocalStoragePluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.storage-local", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.storage.local")
public class DeadmanLocalStoragePluginAutoConfiguration {}
