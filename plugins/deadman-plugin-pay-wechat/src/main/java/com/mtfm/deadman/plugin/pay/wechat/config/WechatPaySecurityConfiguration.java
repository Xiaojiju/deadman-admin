package com.mtfm.deadman.plugin.pay.wechat.config;

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

/**
 * 微信支付回调 Security 配置，允许微信服务器匿名 POST 访问各 Provider 回调 endpoint。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay-wechat", name = "enabled", havingValue = "true")
public class WechatPaySecurityConfiguration {

    private final WechatPayPluginProperties properties;

    /**
     * 构造安全配置。
     *
     * @param properties 微信支付插件配置
     */
    public WechatPaySecurityConfiguration(WechatPayPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 微信支付回调专用安全链，优先级高于用户端 / 管理端 API 链。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(18)
    public SecurityFilterChain wechatPayNotifySecurityFilterChain(HttpSecurity http) throws Exception {
        List<String> endpoints = properties.enabledNotifyEndpoints();
        if (endpoints.isEmpty()) {
            http.securityMatcher("/wechat-pay-notify-disabled-placeholder")
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
