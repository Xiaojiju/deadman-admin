package com.mtfm.deadman.component.client.wechat;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端与微信插件桥接自动配置，仅在 classpath 同时存在 client 与 wechat 时装配。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient")
@ConditionalOnProperty(prefix = "deadman.component.client.wechat", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackageClasses = ClientWechatPhoneBindingHandler.class)
public class ClientWechatIntegrationAutoConfiguration {
}
