package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.security.support.AuthTokenResponseSupport;
import com.mtfm.deadman.security.token.AuthTokenIssueProvider;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 通用 Refresh Token 无感刷新 Filter：按注册的刷新路径匹配对应端的 Provider。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenRefreshFilter extends OncePerRequestFilter {

    private final AuthTokenIssueProviderRegistry providerRegistry;
    private final JsonMapper jsonMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request == null || !HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        return providerRegistry.findByRefreshPath(request.getRequestURI()).isEmpty();
    }

    /**
     * 校验 Refresh Token 并返回新的 Access Token + Refresh Token。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤链（本 Filter 处理完成后不再继续）
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        AuthTokenIssueProvider provider = providerRegistry
                .findByRefreshPath(request.getRequestURI())
                .orElseThrow();
        String refreshToken = AuthTokenResponseSupport.resolveRefreshToken(request, jsonMapper,
                provider.refreshTokenCookieName());
        if (!StringUtils.hasText(refreshToken)) {
            AuthTokenResponseSupport.writeTokenFailure(
                    response, jsonMapper, new BusinessException(ResultCode.TOKEN_INVALID));
            return;
        }
        try {
            AuthTokenVO token = provider.refresh(refreshToken);
            AuthTokenResponseSupport.writeTokenSuccess(response, jsonMapper, token, provider);
        } catch (BusinessException ex) {
            log.debug("realm={} Refresh Token 刷新失败: {}", provider.realm(), ex.getMessage());
            AuthTokenResponseSupport.writeTokenFailure(response, jsonMapper, ex);
        }
    }
}
