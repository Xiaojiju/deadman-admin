package com.mtfm.deadman.support.client.sms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 放行登录验证码发送，避免落到用户端 JWT 链上被要求登录。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
public class ClientSmsPublicSecurityConfiguration {

    /**
     * 匿名访问链，优先级高于用户端过滤链。
     *
     * @param http HttpSecurity
     * @return 过滤链
     * @throws Exception 配置异常
     */
    @Bean
    @Order(16)
    SecurityFilterChain clientSmsPublicFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/client/api/auth/sms/send").csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/client/api/auth/sms/send")
                .permitAll().anyRequest().denyAll());
        return http.build();
    }
}
