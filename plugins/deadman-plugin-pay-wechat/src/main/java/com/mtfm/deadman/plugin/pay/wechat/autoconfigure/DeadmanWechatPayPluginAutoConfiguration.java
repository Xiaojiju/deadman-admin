package com.mtfm.deadman.plugin.pay.wechat.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.mtfm.deadman.plugin.pay.wechat.client.MockWechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGatewayImpl;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;

/**
 * 微信支付插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(WechatPayPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay-wechat", name = "enabled", havingValue = "true")
@ComponentScan(
        basePackages = "com.mtfm.deadman.plugin.pay.wechat",
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.mtfm\\.deadman\\.plugin\\.pay\\.wechat\\.autoconfigure\\..*"))
public class DeadmanWechatPayPluginAutoConfiguration {

    /**
     * 注册微信支付 API 网关，Mock 与真实实现按配置切换。
     *
     * @param properties 插件配置
     * @return API 网关
     */
    @Bean
    WechatPayApiGateway wechatPayApiGateway(WechatPayPluginProperties properties) {
        if (properties.shouldUseMock()) {
            return new MockWechatPayApiGateway();
        }
        return new WechatPayApiGatewayImpl(properties);
    }
}
