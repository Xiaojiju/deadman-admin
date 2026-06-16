package com.mtfm.deadman.component.client.auth.jwt;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.service.ClientUserDetailsService;
import com.mtfm.deadman.security.jwt.RealmJwtAuthenticationFilter;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import org.springframework.stereotype.Component;

/**
 * 用户端 JWT 认证过滤器，委托 {@link RealmJwtAuthenticationFilter} 处理 /client/api 路径。
 */
@Component
public class ClientJwtAuthenticationFilter extends RealmJwtAuthenticationFilter {

    /**
     * @param providerRegistry         Provider 注册表
     * @param clientUserDetailsService 用户端用户详情服务
     */
    public ClientJwtAuthenticationFilter(
            AuthTokenIssueProviderRegistry providerRegistry, ClientUserDetailsService clientUserDetailsService) {
        super(
                providerRegistry,
                ClientAuthConstants.JWT_REALM,
                path -> path != null && path.startsWith("/client/api"),
                clientUserDetailsService,
                "用户端");
    }
}
