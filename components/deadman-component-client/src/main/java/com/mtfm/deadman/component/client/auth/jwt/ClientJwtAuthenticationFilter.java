package com.mtfm.deadman.component.client.auth.jwt;

import java.io.IOException;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.service.ClientUserDetailsService;
import com.mtfm.deadman.security.jwt.RealmJwtAuthenticationFilter;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 用户端 JWT 认证过滤器，委托 {@link RealmJwtAuthenticationFilter} 处理 /client/api 路径。已登出的 Access Token 按未登录处理。
 */
@Component
public class ClientJwtAuthenticationFilter extends RealmJwtAuthenticationFilter {

    private final AuthTokenIssueProviderRegistry providerRegistry;
    private final ClientAccessTokenRevocationStore revocationStore;

    /**
     * @param providerRegistry Provider 注册表
     * @param clientUserDetailsService 用户端用户详情服务
     * @param revocationStore 已登出的 Access Token
     */
    public ClientJwtAuthenticationFilter(AuthTokenIssueProviderRegistry providerRegistry,
        ClientUserDetailsService clientUserDetailsService, ClientAccessTokenRevocationStore revocationStore) {
        super(providerRegistry, ClientAuthConstants.JWT_REALM, path -> path != null && path.startsWith("/client/api"),
            clientUserDetailsService, "用户端");
        this.providerRegistry = providerRegistry;
        this.revocationStore = revocationStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Claims claims = parseAccessClaims(providerRegistry, request.getHeader(HttpHeaders.AUTHORIZATION));
        if (claims != null && revocationStore.isRevoked(claims.getId())) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        super.doFilterInternal(request, response, filterChain);
    }

    /**
     * 解析请求头里的用户端 Access Token。不是用户端 Access Token 或解析失败时返回 null。
     *
     * @param providerRegistry Provider 注册表
     * @param authorization Authorization 请求头
     * @return Claims 或 null
     */
    public static Claims parseAccessClaims(AuthTokenIssueProviderRegistry providerRegistry, String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            var tokenProvider = providerRegistry.require(ClientAuthConstants.JWT_REALM).jwtSupport().tokenProvider();
            Claims claims = tokenProvider.parseClaims(authorization.substring(7));
            if (!tokenProvider.isRealm(claims) || tokenProvider.isRefreshToken(claims)) {
                return null;
            }
            return claims;
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
