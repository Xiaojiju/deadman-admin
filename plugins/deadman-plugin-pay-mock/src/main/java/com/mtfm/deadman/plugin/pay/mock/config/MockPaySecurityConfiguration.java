package com.mtfm.deadman.plugin.pay.mock.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * Mock 支付回调 Security 配置，允许匿名 POST 访问回调 endpoint。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
public class MockPaySecurityConfiguration {

    private final MockPayPluginProperties properties;

    /**
     * 构造安全配置。
     *
     * @param properties Mock 支付插件配置
     */
    public MockPaySecurityConfiguration(MockPayPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * Mock 支付回调专用安全链，优先级高于用户端 / 管理端 API 链。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(18)
    public SecurityFilterChain mockPayNotifySecurityFilterChain(HttpSecurity http) throws Exception {
        String endpoint = properties.getNotifyEndpoint();
        if (!StringUtils.hasText(endpoint)) {
            http.securityMatcher("/mock-pay-notify-disabled-placeholder")
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().denyAll());
            return http.build();
        }
        String normalized = endpoint.trim();
        http.securityMatcher(request -> HttpMethod.POST.matches(request.getMethod())
                        && normalized.equals(request.getRequestURI()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
