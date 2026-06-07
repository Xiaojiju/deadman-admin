package com.mtfm.deadman.component.client.auth.jwt;

import com.mtfm.deadman.component.client.service.ClientUserDetailsService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用户端 JWT 认证过滤器，仅处理 /client/api 路径下的 Bearer 令牌。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientJwtAuthenticationFilter extends OncePerRequestFilter {

    private final ClientJwtTokenProvider clientJwtTokenProvider;
    private final ClientJwtSessionStore clientJwtSessionStore;
    private final ClientUserDetailsService clientUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/client/api");
    }

    /**
     * 解析用户端 JWT 并设置 Security 上下文。
     *
     * @param request     请求
     * @param response    响应
     * @param filterChain 过滤链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = clientJwtTokenProvider.parseClaims(token);
                if (!clientJwtTokenProvider.isClientRealm(claims)) {
                    log.debug("非用户端 JWT realm，跳过用户端认证");
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                Long userId = clientJwtTokenProvider.getUserId(claims);
                String jti = clientJwtTokenProvider.getJti(claims);
                if (!clientJwtSessionStore.isSessionActive(userId, jti)) {
                    log.debug("用户端 JWT 会话已失效 userId={}", userId);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                String userCode = clientJwtTokenProvider.getUserCode(claims);
                UserDetails userDetails = clientUserDetailsService.loadUserByUsername(userCode);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException | JwtException ex) {
                log.debug("用户端 JWT 认证失败: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
