package com.mtfm.deadman.security;

import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.jwt.JwtSessionStore;
import com.mtfm.deadman.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从 Authorization Bearer 解析 JWT，并通过 {@link UserDetailsService} 加载最新用户状态
 * <p>
 * 用户绑定多角色时，角色编码与权限码均会去重合并。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtSessionStore jwtSessionStore;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/client/api");
    }

    /**
     * 从 Authorization Bearer 解析 JWT，并通过 {@link UserDetailsService} 加载最新用户状态。
     * 
     * @param request     请求
     * @param response    响应
     * @param filterChain 过滤链
     * @throws ServletException 如果过滤器内部抛出异常
     * @throws IOException      如果IO操作抛出异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String realm = claims.get("realm", String.class);
                if (realm != null && !AdminAuthConstants.JWT_REALM.equals(realm)) {
                    log.debug("非管理端 JWT realm={}，跳过管理端认证", realm);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                Long userId = jwtTokenProvider.getUserId(claims);
                String jti = jwtTokenProvider.getJti(claims);
                if (!jwtSessionStore.isSessionActive(userId, jti)) {
                    log.debug("JWT 会话已失效 userId={}", userId);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                String userCode = jwtTokenProvider.getUserCode(claims);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userCode);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException | JwtException ex) {
                log.debug("JWT 认证失败: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
