package com.mtfm.deadman.plugin.logistics.kuaidi100.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 快递100 订阅推送 Security 配置，允许快递100 服务器匿名 POST 访问订阅回调 endpoint。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
public class Kuaidi100LogisticsSecurityConfiguration {

    private final Kuaidi100LogisticsPluginProperties properties;

    /**
     * 构造安全配置。
     *
     * @param properties 快递100 插件配置
     */
    public Kuaidi100LogisticsSecurityConfiguration(Kuaidi100LogisticsPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 快递100 订阅推送专用安全链，优先级高于用户端 / 管理端 API 链。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(19)
    public SecurityFilterChain kuaidi100SubscribeNotifySecurityFilterChain(HttpSecurity http) throws Exception {
        String endpoint = properties.resolvedSubscribeNotifyEndpoint();
        http.securityMatcher(request -> HttpMethod.POST.matches(request.getMethod())
                && endpoint.equals(request.getRequestURI()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
