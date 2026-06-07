package com.mtfm.deadman.security;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 将 Spring Security 认证/鉴权失败统一写为 {@link Result} JSON。
 */
@Component
@RequiredArgsConstructor
public class SecurityJsonHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, Result.of(ResultCode.UNAUTHORIZED));
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {
        writeResult(response, HttpServletResponse.SC_FORBIDDEN, Result.of(ResultCode.FORBIDDEN));
    }

    private void writeResult(HttpServletResponse response, int httpStatus, Result<Void> body) throws IOException {
        response.setStatus(httpStatus);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), body);
    }
}
