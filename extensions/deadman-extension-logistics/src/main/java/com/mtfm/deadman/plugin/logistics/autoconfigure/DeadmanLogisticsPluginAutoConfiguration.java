package com.mtfm.deadman.plugin.logistics.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.plugin.logistics.config.LogisticsPluginProperties;

/**
 * 物流能力延伸自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(LogisticsPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.logistics", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.logistics")
public class DeadmanLogisticsPluginAutoConfiguration {}
