package com.mtfm.deadman.security;

import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.jwt.RealmJwtAuthenticationFilter;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * 管理端 JWT 认证过滤器，委托 {@link RealmJwtAuthenticationFilter} 处理非 /client/api 路径。
 */
@Component
public class JwtAuthenticationFilter extends RealmJwtAuthenticationFilter {

    /**
     * @param providerRegistry   Provider 注册表
     * @param userDetailsService 管理端用户详情服务
     */
    public JwtAuthenticationFilter(
            AuthTokenIssueProviderRegistry providerRegistry, UserDetailsService userDetailsService) {
        super(
                providerRegistry,
                AdminAuthConstants.JWT_REALM,
                path -> path != null && !path.startsWith("/client/api"),
                userDetailsService,
                "管理端");
    }
}
