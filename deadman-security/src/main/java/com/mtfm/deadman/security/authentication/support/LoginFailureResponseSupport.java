package com.mtfm.deadman.security.authentication.support;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 登录失败统一 JSON 响应（HTTP 200 + 业务码，与现有登录接口约定一致）。
 */
public final class LoginFailureResponseSupport {

    private LoginFailureResponseSupport() {
    }

    /**
     * 将登录失败写入 JSON 响应。
     *
     * @param response  HTTP 响应
     * @param jsonMapper JSON 映射器
     * @param exception 认证异常
     */
    public static void writeLoginFailure(
            HttpServletResponse response, JsonMapper jsonMapper, AuthenticationException exception)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), resolveResult(exception));
    }

    /**
     * 将 Spring Security 认证异常映射为业务 Result。
     *
     * @param exception 认证异常
     * @return 业务 Result
     */
    public static Result<Void> resolveResult(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return Result.of(ResultCode.PASSWORD_MISMATCH);
        }
        if (exception instanceof DisabledException disabledException) {
            return Result.of(ResultCode.FORBIDDEN.getCode(), disabledException.getMessage());
        }
        return Result.of(ResultCode.UNAUTHORIZED);
    }
}
