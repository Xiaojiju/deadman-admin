package com.mtfm.deadman.security.authentication;

import com.mtfm.deadman.security.authentication.support.LoginFailureResponseSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

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
        LoginFailureResponseSupport.writeLoginFailure(response, jsonMapper, exception);
    }
}
