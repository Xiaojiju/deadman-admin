package com.mtfm.deadman.plugin.wechat.login.config;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginApiConstants;
import com.mtfm.deadman.plugin.wechat.web.WechatWebConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 微信登录统一公开接口 Security 配置。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
public class WechatLoginSecurityConfiguration {

    /**
     * 放行微信登录公开 API。
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(15)
    public SecurityFilterChain wechatLoginPublicAccessFilterChain(HttpSecurity http) throws Exception {
        String publicPattern = WechatLoginApiConstants.API_BASE_PATH + "/**";
        String legacyAuthorizePath = WechatWebConstants.AUTHORIZE_URL_API_PATH;
        http.securityMatcher(publicPattern, legacyAuthorizePath)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                        HttpMethod.GET, WechatLoginApiConstants.API_BASE_PATH + "/kinds")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, WechatLoginApiConstants.API_BASE_PATH + "/*/initiate")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, WechatLoginApiConstants.API_BASE_PATH + "/sessions")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, legacyAuthorizePath)
                        .permitAll()
                        .anyRequest()
                        .denyAll());
        return http.build();
    }
}
