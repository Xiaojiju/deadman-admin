package com.mtfm.deadman.plugin.wechat.miniprogram.autoconfigure;

import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramPluginProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 微信插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(WechatMiniprogramPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-miniprogram", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = {
        "com.mtfm.deadman.plugin.wechat.miniprogram",
        "com.mtfm.deadman.plugin.wechat.common"
})
public class DeadmanWechatPluginAutoConfiguration {
}
