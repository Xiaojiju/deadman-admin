package com.mtfm.deadman.plugin.pay.alipay.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.mtfm.deadman.plugin.pay.alipay.config.AlipayPayPluginProperties;

/**
 * 支付宝支付插件自动配置（默认关闭；实现 SPI 后由业务门面接入）。
 */
@AutoConfiguration
@EnableConfigurationProperties(AlipayPayPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay-alipay", name = "enabled", havingValue = "true")
@ComponentScan(
        basePackages = "com.mtfm.deadman.plugin.pay.alipay",
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.mtfm\\.deadman\\.plugin\\.pay\\.alipay\\.autoconfigure\\..*"))
public class DeadmanAlipayPayPluginAutoConfiguration {
}
