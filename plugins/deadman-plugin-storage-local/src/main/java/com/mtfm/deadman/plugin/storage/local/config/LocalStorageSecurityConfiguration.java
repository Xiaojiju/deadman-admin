package com.mtfm.deadman.plugin.storage.local.config;

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
 * 本地存储公开访问 Security 配置，允许匿名 GET 访问静态文件 URL。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.plugin.storage-local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalStorageSecurityConfiguration {

    private final LocalStoragePluginProperties properties;

    /**
     * 构造安全配置。
     *
     * @param properties 本地存储配置
     */
    public LocalStorageSecurityConfiguration(LocalStoragePluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 公开文件访问安全链，优先级高于管理端 API 链。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(15)
    public SecurityFilterChain localFilePublicAccessFilterChain(HttpSecurity http) throws Exception {
        String publicPattern = normalizePrefix(properties.getPublicUrlPrefix()) + "/**";
        http.securityMatcher(publicPattern)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.GET, publicPattern)
                        .permitAll()
                        .anyRequest()
                        .denyAll());
        return http.build();
    }

    private static String normalizePrefix(String prefix) {
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix.endsWith("/")) {
            return prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }
}
