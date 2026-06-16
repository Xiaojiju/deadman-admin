package com.mtfm.deadman.security.support;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.security.token.AuthTokenIssueProvider;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 认证令牌响应写入与 Refresh Token Cookie 解析工具。
 */
public final class AuthTokenResponseSupport {

    private AuthTokenResponseSupport() {
    }

    /**
     * 写入登录/刷新成功响应，并按端策略写入 Refresh Token HttpOnly Cookie。
     *
     * @param response   HTTP 响应
     * @param jsonMapper JSON 映射器
     * @param token      双令牌视图
     * @param provider   端 Provider（Cookie 策略）
     * @throws IOException 写入失败
     */
    public static void writeTokenSuccess(
            HttpServletResponse response, JsonMapper jsonMapper, AuthTokenVO token, AuthTokenIssueProvider provider)
            throws IOException {
        writeRefreshTokenCookie(
                response,
                token.getRefreshToken(),
                token.getRefreshExpiresIn(),
                provider.refreshTokenCookieName(),
                provider.refreshTokenCookiePath(),
                provider.isRefreshCookieSecure());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), Result.ok(token));
    }

    /**
     * 写入令牌刷新失败响应。
     *
     * @param response   HTTP 响应
     * @param jsonMapper JSON 映射器
     * @param ex         业务异常
     * @throws IOException 写入失败
     */
    public static void writeTokenFailure(
            HttpServletResponse response, JsonMapper jsonMapper, BusinessException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), Result.of(ex.getCode(), ex.getMessage()));
    }

    /**
     * 从 Cookie、Authorization 或请求体 JSON 字段解析 Refresh Token。
     *
     * @param request    HTTP 请求
     * @param jsonMapper JSON 映射器
     * @param cookieName Refresh Cookie 名称
     * @return Refresh Token
     */
    public static String resolveRefreshToken(HttpServletRequest request, JsonMapper jsonMapper, String cookieName) {
        String cookieToken = readRefreshTokenFromCookie(request, cookieName);
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return readRefreshTokenFromBody(request, jsonMapper);
    }

    private static void writeRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            Long refreshExpiresIn,
            String cookieName,
            String cookiePath,
            boolean secure) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }
        long maxAge = refreshExpiresIn != null ? refreshExpiresIn : Duration.ofDays(7).toSeconds();
        ResponseCookie cookie = ResponseCookie.from(cookieName, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path(cookiePath)
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private static String readRefreshTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static String readRefreshTokenFromBody(HttpServletRequest request, JsonMapper jsonMapper) {
        try {
            RefreshTokenBody body = jsonMapper.readValue(request.getInputStream(), RefreshTokenBody.class);
            return body != null ? body.refreshToken() : null;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Refresh Token 请求体。
     *
     * @param refreshToken Refresh Token
     */
    public record RefreshTokenBody(String refreshToken) {
    }
}
