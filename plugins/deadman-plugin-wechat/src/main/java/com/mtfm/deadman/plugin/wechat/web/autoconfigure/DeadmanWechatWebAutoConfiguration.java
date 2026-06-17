package com.mtfm.deadman.plugin.wechat.web.autoconfigure;

import com.mtfm.deadman.plugin.wechat.web.config.WechatWebPluginProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 微信网页扫码登录插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(WechatWebPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-web", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.wechat.web")
public class DeadmanWechatWebAutoConfiguration {
}
