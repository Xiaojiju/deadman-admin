package com.mtfm.deadman.plugin.ess.tencent.config;

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
 * 腾讯电子签回调 Security 配置：允许电子签服务器匿名 POST 回调 endpoint。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.plugin.ess-tencent", name = "enabled", havingValue = "true")
public class EssTencentCallbackSecurityConfiguration {

    private final EssTencentPluginProperties properties;

    /**
     * 构造安全配置。
     *
     * @param properties 插件配置
     */
    public EssTencentCallbackSecurityConfiguration(EssTencentPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 电子签回调专用安全链，优先级高于管理端 / 用户端 API 链。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(17)
    public SecurityFilterChain essTencentCallbackSecurityFilterChain(HttpSecurity http) throws Exception {
        String endpoint = properties.resolveCallbackEndpoint();
        http.securityMatcher(request -> HttpMethod.POST.matches(request.getMethod())
                        && endpoint.equals(request.getRequestURI()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
