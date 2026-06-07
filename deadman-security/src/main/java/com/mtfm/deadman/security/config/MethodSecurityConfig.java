package com.mtfm.deadman.security.config;

import com.mtfm.deadman.security.LoginUser;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.core.Authentication;

/**
 * 方法级权限：持有 SUPER_ADMIN 角色的用户跳过 {@code @PreAuthorize} 权限码校验。
 */
@Configuration
public class MethodSecurityConfig {

    /**
     * 方法级权限：持有 SUPER_ADMIN 角色的用户跳过 {@code @PreAuthorize} 权限码校验。
     * 
     * @param preAuthorizeAuthorizationManager 权限管理器
     * @return 权限管理器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor preAuthorizeAdvisor(AuthorizationManager<MethodInvocation> preAuthorizeAuthorizationManager) {
        return AuthorizationManagerBeforeMethodInterceptor.preAuthorize(preAuthorizeAuthorizationManager);
    }

    /**
     * 方法级权限：持有 SUPER_ADMIN 角色的用户跳过 {@code @PreAuthorize} 权限码校验。
     * 
     * @return 权限管理器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static AuthorizationManager<MethodInvocation> preAuthorizeAuthorizationManager() {
        PreAuthorizeAuthorizationManager delegate = new PreAuthorizeAuthorizationManager();
        return (authentication, invocation) -> {
            Authentication auth = authentication.get();
            if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser && loginUser.isSuperAdmin()) {
                return new AuthorizationDecision(true);
            }
            return delegate.authorize(authentication, invocation);
        };
    }
}
