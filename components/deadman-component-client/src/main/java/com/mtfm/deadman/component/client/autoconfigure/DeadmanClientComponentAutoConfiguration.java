package com.mtfm.deadman.component.client.autoconfigure;

import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端组件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(ClientComponentProperties.class)
@ConditionalOnProperty(prefix = "deadman.component.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@MapperScan("com.mtfm.deadman.component.client.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.component.client")
public class DeadmanClientComponentAutoConfiguration {
}
