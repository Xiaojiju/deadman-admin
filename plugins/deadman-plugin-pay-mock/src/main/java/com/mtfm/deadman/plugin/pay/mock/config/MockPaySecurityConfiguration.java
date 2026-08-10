package com.mtfm.deadman.plugin.pay.mock.config;

import java.util.ArrayList;
import java.util.List;

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
 * Mock 支付/退款回调 Security 配置，允许匿名 POST 访问回调 endpoint。
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
     * Mock 支付/退款回调专用安全链，优先级高于用户端 / 管理端 API 链。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(18)
    public SecurityFilterChain mockPayNotifySecurityFilterChain(HttpSecurity http) throws Exception {
        List<String> endpoints = new ArrayList<>();
        if (StringUtils.hasText(properties.getNotifyEndpoint())) {
            endpoints.add(properties.getNotifyEndpoint().trim());
        }
        if (StringUtils.hasText(properties.getRefundNotifyEndpoint())) {
            endpoints.add(properties.getRefundNotifyEndpoint().trim());
        }
        if (StringUtils.hasText(properties.getTransferNotifyEndpoint())) {
            endpoints.add(properties.getTransferNotifyEndpoint().trim());
        }
        if (endpoints.isEmpty()) {
            http.securityMatcher("/mock-pay-notify-disabled-placeholder")
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().denyAll());
            return http.build();
        }
        http.securityMatcher(request -> HttpMethod.POST.matches(request.getMethod())
                        && endpoints.stream().anyMatch(endpoint -> endpoint.equals(request.getRequestURI())))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
