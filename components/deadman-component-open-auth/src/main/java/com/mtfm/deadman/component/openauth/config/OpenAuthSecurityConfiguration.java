package com.mtfm.deadman.component.openauth.config;

import com.mtfm.deadman.component.openauth.constant.OpenAuthConstants;
import com.mtfm.deadman.security.SecurityJsonHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 开放 API 安全过滤链，放行 /open-api/**（凭 client_secret 鉴权，不走 JWT）。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.component.open-auth", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class OpenAuthSecurityConfiguration {

    private final SecurityJsonHandlers securityJsonHandlers;

    /**
     * 开放 API 过滤链，仅匹配 /open-api/**。
     *
     * @param http HttpSecurity
     * @return 过滤链
     */
    @Bean
    @Order(10)
    SecurityFilterChain openAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(OpenAuthConstants.OPEN_API_PREFIX + "/**");
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(securityJsonHandlers)
                        .accessDeniedHandler(securityJsonHandlers));
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(HttpMethod.POST, OpenAuthConstants.TOKEN_ENDPOINT).permitAll();
            auth.requestMatchers("/error").permitAll();
            auth.anyRequest().denyAll();
        });
        return http.build();
    }
}
