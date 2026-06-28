package com.mtfm.deadman.component.openauth.autoconfigure;

import com.mtfm.deadman.component.openauth.config.OpenAuthComponentProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 开放授权组件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenAuthComponentProperties.class)
@ConditionalOnProperty(prefix = "deadman.component.open-auth", name = "enabled", havingValue = "true", matchIfMissing = true)
@MapperScan("com.mtfm.deadman.component.openauth.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.component.openauth")
public class DeadmanOpenAuthAutoConfiguration {
}
