package com.mtfm.deadman.plugin.websocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * WebSocket 握手路径放行，具体身份在握手拦截器 + {@link com.mtfm.deadman.plugin.websocket.spi.WebSocketAuthenticator} 中校验。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.plugin.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketSecurityConfiguration {

  @Bean
    @Order(0)
    SecurityFilterChain webSocketSecurityFilterChain(HttpSecurity http, WebSocketPluginProperties properties) throws Exception {
        String pattern = properties.getEndpointPath() + "/**";
        http.securityMatcher(pattern)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
