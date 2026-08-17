package com.mtfm.deadman.security.authentication.support;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
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
     * <p>
     * 微信绑定注册等场景会把 {@link BusinessException} 包装为 {@link BadCredentialsException}，
     * 需还原真实业务码，避免一律显示「用户名或密码错误」。
     *
     * @param exception 认证异常
     * @return 业务 Result
     */
    public static Result<Void> resolveResult(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            BusinessException businessCause = findBusinessCause(exception);
            if (businessCause != null) {
                return Result.of(businessCause.getCode(), businessCause.getMessage());
            }
            String message = exception.getMessage();
            if (StringUtils.hasText(message) && !isGenericCredentialMessage(message)) {
                return Result.of(ResultCode.BAD_REQUEST.getCode(), message);
            }
            return Result.of(ResultCode.PASSWORD_MISMATCH);
        }
        if (exception instanceof DisabledException disabledException) {
            return Result.of(ResultCode.FORBIDDEN.getCode(), disabledException.getMessage());
        }
        return Result.of(ResultCode.UNAUTHORIZED);
    }

    /**
     * 从异常链中提取业务异常。
     *
     * @param exception 认证异常
     * @return 业务异常，不存在时返回 null
     */
    private static BusinessException findBusinessCause(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof BusinessException businessException) {
                return businessException;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * 是否为通用的用户名密码错误文案（需映射为 PASSWORD_MISMATCH）。
     *
     * @param message 异常消息
     * @return 是通用文案时返回 true
     */
    private static boolean isGenericCredentialMessage(String message) {
        return "用户名或密码错误".equals(message)
            || "用户名或密码不能为空".equals(message)
            || "密码不能为空".equals(message);
    }
}
