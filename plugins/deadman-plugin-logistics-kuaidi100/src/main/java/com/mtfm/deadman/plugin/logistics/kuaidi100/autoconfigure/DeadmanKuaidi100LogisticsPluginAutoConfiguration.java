package com.mtfm.deadman.plugin.logistics.kuaidi100.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.mtfm.deadman.plugin.logistics.kuaidi100.client.Kuaidi100LogisticsApiGateway;
import com.mtfm.deadman.plugin.logistics.kuaidi100.client.Kuaidi100LogisticsApiGatewayImpl;
import com.mtfm.deadman.plugin.logistics.kuaidi100.client.MockKuaidi100LogisticsApiGateway;
import com.mtfm.deadman.plugin.logistics.kuaidi100.config.Kuaidi100LogisticsPluginProperties;

/**
 * 快递100 物流插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(Kuaidi100LogisticsPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
@ComponentScan(
        basePackages = "com.mtfm.deadman.plugin.logistics.kuaidi100",
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.mtfm\\.deadman\\.plugin\\.logistics\\.kuaidi100\\.autoconfigure\\..*"))
public class DeadmanKuaidi100LogisticsPluginAutoConfiguration {

    /**
     * 注册快递100 API 网关，Mock 与真实实现按配置切换。
     *
     * @param properties 插件配置
     * @return API 网关
     */
    @Bean
    Kuaidi100LogisticsApiGateway kuaidi100LogisticsApiGateway(Kuaidi100LogisticsPluginProperties properties) {
        if (properties.shouldUseMock()) {
            return new MockKuaidi100LogisticsApiGateway();
        }
        return new Kuaidi100LogisticsApiGatewayImpl(properties);
    }
}
