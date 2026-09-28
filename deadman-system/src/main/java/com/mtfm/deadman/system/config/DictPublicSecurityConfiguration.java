package com.mtfm.deadman.system.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 放行客户端字典查询，避免落到用户端 JWT 链上被要求登录。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
public class DictPublicSecurityConfiguration {

    /**
     * 匿名访问链，优先级高于用户端过滤链。
     *
     * @param http HttpSecurity
     * @return 过滤链
     * @throws Exception 配置异常
     */
    @Bean
    @Order(14)
    SecurityFilterChain dictPublicFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/client/api/dicts")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.GET, "/client/api/dicts")
                        .permitAll()
                        .anyRequest()
                        .denyAll());
        return http.build();
    }
}
