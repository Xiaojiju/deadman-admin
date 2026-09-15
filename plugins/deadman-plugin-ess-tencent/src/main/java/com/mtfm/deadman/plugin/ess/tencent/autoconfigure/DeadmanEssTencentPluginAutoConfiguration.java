package com.mtfm.deadman.plugin.ess.tencent.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.plugin.ess.tencent.client.EssClientFactory;
import com.mtfm.deadman.plugin.ess.tencent.client.EssbasicClientFactory;
import com.mtfm.deadman.plugin.ess.tencent.client.TencentEssApiGateway;
import com.mtfm.deadman.plugin.ess.tencent.client.TencentEssApiGatewayImpl;
import com.mtfm.deadman.plugin.ess.tencent.client.TencentEssbasicApiGateway;
import com.mtfm.deadman.plugin.ess.tencent.client.TencentEssbasicApiGatewayImpl;
import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;

/**
 * 腾讯电子签插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(EssTencentPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.ess-tencent", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.ess.tencent")
public class DeadmanEssTencentPluginAutoConfiguration {

    /**
     * 注册 EssClient 工厂。
     *
     * @param properties 插件配置
     * @return Client 工厂
     */
    @Bean
    EssClientFactory essClientFactory(EssTencentPluginProperties properties) {
        return new EssClientFactory(properties);
    }

    /**
     * 注册腾讯电子签 API 网关。
     *
     * @param essClientFactory Client 工厂
     * @return API 网关
     */
    @Bean
    TencentEssApiGateway tencentEssApiGateway(EssClientFactory essClientFactory) {
        return new TencentEssApiGatewayImpl(essClientFactory);
    }

    /**
     * 注册渠道版 EssbasicClient 工厂。
     *
     * @param properties 插件配置
     * @return 渠道版 Client 工厂
     */
    @Bean
    EssbasicClientFactory essbasicClientFactory(EssTencentPluginProperties properties) {
        return new EssbasicClientFactory(properties);
    }

    /**
     * 注册腾讯电子签渠道版 API 网关。
     *
     * @param essbasicClientFactory 渠道版 Client 工厂
     * @return 渠道版 API 网关
     */
    @Bean
    TencentEssbasicApiGateway tencentEssbasicApiGateway(
            EssbasicClientFactory essbasicClientFactory, EssTencentPluginProperties properties) {
        return new TencentEssbasicApiGatewayImpl(essbasicClientFactory, properties);
    }
}
