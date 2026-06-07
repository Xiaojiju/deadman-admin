package com.mtfm.deadman.security.config;

import com.mtfm.deadman.security.JwtAuthenticationFilter;
import com.mtfm.deadman.security.SecurityJsonHandlers;
import com.mtfm.deadman.security.authentication.JwtLoginFailureHandler;
import com.mtfm.deadman.security.authentication.JwtLoginSuccessHandler;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroup;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroupManager;
import com.mtfm.deadman.security.authentication.provider.LoginProviderSecuritySupport;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security：JWT 无状态、统一 Provider 登录 Filter、统一 JSON 401/403。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityJsonHandlers securityJsonHandlers;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final JwtLoginFailureHandler jwtLoginFailureHandler;
    private final LoginProviderGroupManager loginProviderGroupManager;

    /**
     * 管理端安全过滤链，匹配 /api/**。
     *
     * @param http HttpSecurity
     * @return 过滤链
     */
    @Bean
    @Order(100)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LoginProviderGroup adminGroup = loginProviderGroupManager.requireGroup(AdminAuthConstants.LOGIN_GROUP_ID);
        AuthenticationManager adminAuthenticationManager =
                loginProviderGroupManager.requireAuthenticationManager(AdminAuthConstants.LOGIN_GROUP_ID);

        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(adminAuthenticationManager)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(securityJsonHandlers)
                        .accessDeniedHandler(securityJsonHandlers))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/api/components")
                            .permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/register")
                            .permitAll();
                    for (var provider : loginProviderGroupManager.listProviders(AdminAuthConstants.LOGIN_GROUP_ID)) {
                        auth.requestMatchers(
                                        HttpMethod.POST, provider.resolveLoginEndpoint(adminGroup))
                                .permitAll();
                    }
                    auth.requestMatchers("/error").permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        LoginProviderSecuritySupport.registerProviderLoginFilters(
                http,
                adminGroup,
                loginProviderGroupManager,
                adminAuthenticationManager,
                jwtLoginSuccessHandler,
                jwtLoginFailureHandler);

        return http.build();
    }
}
