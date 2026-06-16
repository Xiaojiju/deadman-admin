package com.mtfm.deadman.support.client.wechat.autoconfigure;

import com.mtfm.deadman.component.client.autoconfigure.DeadmanClientComponentAutoConfiguration;
import com.mtfm.deadman.plugin.wechat.miniprogram.autoconfigure.DeadmanWechatPluginAutoConfiguration;
import com.mtfm.deadman.support.client.wechat.config.ClientWechatSupportProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端微信桥接自动配置，在 client 组件与 wechat 插件同时存在时装配。
 */
@AutoConfiguration(after = {DeadmanClientComponentAutoConfiguration.class, DeadmanWechatPluginAutoConfiguration.class})
@ConditionalOnClass(name = "com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient")
@ConditionalOnProperty(prefix = "deadman.support.client-wechat", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientWechatSupportProperties.class)
@ComponentScan(basePackages = "com.mtfm.deadman.support.client.wechat")
public class DeadmanClientWechatSupportAutoConfiguration {

    /**
     * 注册用户端微信登录 Provider，覆盖插件默认 client 实现。
     *
     * @return Bean 定义注册后处理器
     */
    @Bean
    public static ClientWechatLoginProviderRegistrar clientWechatLoginProviderRegistrar() {
        return new ClientWechatLoginProviderRegistrar();
    }
}
