package com.mtfm.deadman.plugin.pay.mock.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.mtfm.deadman.plugin.pay.mock.config.MockPayPluginProperties;

/**
 * Mock 支付插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(MockPayPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
@ComponentScan(
        basePackages = "com.mtfm.deadman.plugin.pay.mock",
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.mtfm\\.deadman\\.plugin\\.pay\\.mock\\.autoconfigure\\..*"))
public class DeadmanMockPayPluginAutoConfiguration {
}
