package com.mtfm.deadman.plugin.im.tencent.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.plugin.im.tencent.client.MockTencentImApiGateway;
import com.mtfm.deadman.plugin.im.tencent.client.TencentImApiGateway;
import com.mtfm.deadman.plugin.im.tencent.client.TencentImApiGatewayImpl;
import com.mtfm.deadman.plugin.im.tencent.client.TencentImUserSigGenerator;
import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;

import tools.jackson.databind.json.JsonMapper;

/**
 * 腾讯云 IM 插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(ImTencentPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.im-tencent", name = "enabled", havingValue = "true", matchIfMissing = true)
@MapperScan("com.mtfm.deadman.plugin.im.tencent.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.im.tencent")
public class DeadmanImTencentPluginAutoConfiguration {

    /**
     * 注册腾讯云 IM API 网关，Mock 与真实实现按配置切换。
     *
     * @param properties 插件配置
     * @param jsonMapper JSON 映射器
     * @return API 网关
     */
    @Bean
    TencentImApiGateway tencentImApiGateway(ImTencentPluginProperties properties, JsonMapper jsonMapper) {
        if (properties.shouldUseMock()) {
            return new MockTencentImApiGateway();
        }
        TencentImUserSigGenerator userSigGenerator = new TencentImUserSigGenerator(properties, jsonMapper);
        return new TencentImApiGatewayImpl(properties, userSigGenerator, jsonMapper);
    }
}
