package com.mtfm.deadman.support.wechat.autoconfigure;

import com.mtfm.deadman.plugin.wechat.login.autoconfigure.DeadmanWechatLoginAutoConfiguration;
import com.mtfm.deadman.plugin.wechat.miniprogram.autoconfigure.DeadmanWechatPluginAutoConfiguration;
import com.mtfm.deadman.plugin.wechat.web.autoconfigure.DeadmanWechatWebAutoConfiguration;
import com.mtfm.deadman.support.wechat.config.AdminWechatSupportProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 管理端微信桥接自动配置，在 wechat 插件与 system/security 同时存在时装配。
 */
@AutoConfiguration(after = {
        DeadmanWechatPluginAutoConfiguration.class,
        DeadmanWechatWebAutoConfiguration.class,
        DeadmanWechatLoginAutoConfiguration.class
})
@ConditionalOnClass(name = "com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient")
@ConditionalOnProperty(prefix = "deadman.support.wechat", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AdminWechatSupportProperties.class)
@ComponentScan(basePackages = "com.mtfm.deadman.support.wechat")
public class DeadmanWechatSupportAutoConfiguration {

    /**
     * 注册管理端微信登录 Provider，覆盖插件默认 admin 实现。
     *
     * @return Bean 定义注册后处理器
     */
    @Bean
    public static AdminWechatLoginProviderRegistrar adminWechatLoginProviderRegistrar() {
        return new AdminWechatLoginProviderRegistrar();
    }

    /**
     * 注册管理端微信网页扫码登录 Provider，覆盖插件默认 admin 实现。
     *
     * @return Bean 定义注册后处理器
     */
    @Bean
    public static AdminWechatWebLoginProviderRegistrar adminWechatWebLoginProviderRegistrar() {
        return new AdminWechatWebLoginProviderRegistrar();
    }
}
