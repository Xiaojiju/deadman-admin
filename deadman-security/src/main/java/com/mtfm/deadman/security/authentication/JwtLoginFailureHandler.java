package com.mtfm.deadman.security.authentication;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 登录失败时返回统一 JSON（与业务异常风格一致）。
 */
@Component
@RequiredArgsConstructor
public class JwtLoginFailureHandler implements AuthenticationFailureHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        Result<Void> body = resolveBody(exception);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), body);
    }

    private Result<Void> resolveBody(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return Result.of(ResultCode.PASSWORD_MISMATCH);
        }
        if (exception instanceof DisabledException disabledException) {
            return Result.of(ResultCode.FORBIDDEN.getCode(), disabledException.getMessage());
        }
        return Result.of(ResultCode.UNAUTHORIZED);
    }
}
