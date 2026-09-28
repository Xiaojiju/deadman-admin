package com.mtfm.deadman.plugin.identity.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.mybatis.spring.annotation.MapperScan;

import com.mtfm.deadman.plugin.identity.client.TencentIaiFaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.client.UnconfiguredFaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.config.IdentityPluginProperties;
import com.mtfm.deadman.plugin.identity.config.OnTencentIaiCredentialsCondition;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionClient;

/**
 * 实名认证插件自动配置。{@code deadman.plugin.identity.enabled=false} 时不装配。
 */
@AutoConfiguration
@EnableConfigurationProperties(IdentityPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.identity", name = "enabled", havingValue = "true")
@MapperScan("com.mtfm.deadman.plugin.identity.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.identity")
public class DeadmanIdentityPluginAutoConfiguration {

    /**
     * 密钥齐全时使用腾讯云人脸识别。
     *
     * @param properties 插件配置
     * @return 腾讯云实现
     */
    @Bean
    @Conditional(OnTencentIaiCredentialsCondition.class)
    FaceRecognitionClient tencentIaiFaceRecognitionClient(IdentityPluginProperties properties) {
        return new TencentIaiFaceRecognitionClient(properties);
    }

    /**
     * 未配置密钥时的占位实现，调用时返回配置错误。
     *
     * @return 占位实现
     */
    @Bean
    @ConditionalOnMissingBean(FaceRecognitionClient.class)
    FaceRecognitionClient unconfiguredFaceRecognitionClient() {
        return new UnconfiguredFaceRecognitionClient();
    }
}
