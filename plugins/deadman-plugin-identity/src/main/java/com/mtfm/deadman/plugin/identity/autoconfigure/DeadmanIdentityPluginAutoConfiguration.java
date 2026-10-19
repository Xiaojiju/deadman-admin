package com.mtfm.deadman.plugin.identity.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.mybatis.spring.annotation.MapperScan;

<<<<<<< HEAD
import com.mtfm.deadman.plugin.identity.client.MockFaceRecognitionClient;
=======
>>>>>>> refs/remotes/origin/develop-1.0.1
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
<<<<<<< HEAD
     * 未接入腾讯云时的联调实现。打开后不再装配真实通道和报错占位。
     *
     * @return 直接通过的人脸通道
     */
    @Bean
    @ConditionalOnProperty(prefix = "deadman.plugin.identity", name = "mock", havingValue = "true")
    FaceRecognitionClient mockFaceRecognitionClient() {
        return new MockFaceRecognitionClient();
    }

    /**
     * 密钥齐全且未打开 mock 时使用腾讯云人脸识别。
=======
     * 密钥齐全时使用腾讯云人脸识别。
>>>>>>> refs/remotes/origin/develop-1.0.1
     *
     * @param properties 插件配置
     * @return 腾讯云实现
     */
    @Bean
    @Conditional(OnTencentIaiCredentialsCondition.class)
<<<<<<< HEAD
    @ConditionalOnProperty(prefix = "deadman.plugin.identity", name = "mock", havingValue = "false",
        matchIfMissing = true)
=======
>>>>>>> refs/remotes/origin/develop-1.0.1
    FaceRecognitionClient tencentIaiFaceRecognitionClient(IdentityPluginProperties properties) {
        return new TencentIaiFaceRecognitionClient(properties);
    }

    /**
<<<<<<< HEAD
     * 未配置密钥且未打开 mock 时的占位实现，调用时返回配置错误。
=======
     * 未配置密钥时的占位实现，调用时返回配置错误。
>>>>>>> refs/remotes/origin/develop-1.0.1
     *
     * @return 占位实现
     */
    @Bean
    @ConditionalOnMissingBean(FaceRecognitionClient.class)
<<<<<<< HEAD
    @ConditionalOnProperty(prefix = "deadman.plugin.identity", name = "mock", havingValue = "false",
        matchIfMissing = true)
=======
>>>>>>> refs/remotes/origin/develop-1.0.1
    FaceRecognitionClient unconfiguredFaceRecognitionClient() {
        return new UnconfiguredFaceRecognitionClient();
    }
}
