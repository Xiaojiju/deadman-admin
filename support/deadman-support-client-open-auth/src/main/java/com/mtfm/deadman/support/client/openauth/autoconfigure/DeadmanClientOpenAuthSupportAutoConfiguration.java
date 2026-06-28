package com.mtfm.deadman.support.client.openauth.autoconfigure;

import com.mtfm.deadman.component.client.autoconfigure.DeadmanClientComponentAutoConfiguration;
import com.mtfm.deadman.component.openauth.autoconfigure.DeadmanOpenAuthAutoConfiguration;
import com.mtfm.deadman.support.client.openauth.config.ClientOpenAuthSupportProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端开放授权桥接自动配置。
 */
@AutoConfiguration(after = {DeadmanClientComponentAutoConfiguration.class, DeadmanOpenAuthAutoConfiguration.class})
@ConditionalOnProperty(prefix = "deadman.support.client-open-auth", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientOpenAuthSupportProperties.class)
@ComponentScan(basePackages = "com.mtfm.deadman.support.client.openauth")
public class DeadmanClientOpenAuthSupportAutoConfiguration {
}
