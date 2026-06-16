package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.security.token.AuthTokenIssueProvider;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * 按 realm 解析 Bearer Access Token 并设置 Security 上下文。
 */
@Slf4j
public class RealmJwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenIssueProviderRegistry providerRegistry;
    private final String realm;
    private final Predicate<String> pathMatcher;
    private final UserDetailsService userDetailsService;
    private final String logLabel;

    /**
     * @param providerRegistry Provider 注册表
     * @param realm            端标识
     * @param pathMatcher      返回 true 表示本 Filter 处理该请求路径
     * @param userDetailsService 用户详情服务
     * @param logLabel         日志前缀（如「管理端」「用户端」）
     */
    public RealmJwtAuthenticationFilter(
            AuthTokenIssueProviderRegistry providerRegistry,
            String realm,
            Predicate<String> pathMatcher,
            UserDetailsService userDetailsService,
            String logLabel) {
        this.providerRegistry = providerRegistry;
        this.realm = realm;
        this.pathMatcher = pathMatcher;
        this.userDetailsService = userDetailsService;
        this.logLabel = logLabel;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !pathMatcher.test(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        try {
            AuthTokenIssueProvider provider = providerRegistry.require(realm);
            RealmJwtTokenProvider tokenProvider = provider.jwtSupport().tokenProvider();
            Claims claims = tokenProvider.parseClaims(token);
            if (!tokenProvider.isRealm(claims)) {
                log.debug("非{} JWT realm，跳过认证", logLabel);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            if (tokenProvider.isRefreshToken(claims)) {
                log.debug("{} Refresh Token 不可用于业务接口鉴权", logLabel);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            Long userId = tokenProvider.getUserId(claims);
            String jti = tokenProvider.getJti(claims);
            if (!provider.jwtSupport().sessionStore().isSessionActive(userId, jti)) {
                log.debug("{} JWT 会话已失效 userId={}", logLabel, userId);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            String userCode = tokenProvider.getUserCode(claims);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userCode);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException | JwtException ex) {
            log.debug("{} JWT 认证失败: {}", logLabel, ex.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
