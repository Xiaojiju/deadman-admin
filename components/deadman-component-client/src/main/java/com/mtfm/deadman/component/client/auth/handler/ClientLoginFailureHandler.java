package com.mtfm.deadman.component.client.auth.handler;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.spi.ClientLoginProvider;
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
import java.util.List;

/**
 * 用户端登录失败处理器，统一 JSON 响应并触发失败回调 SPI。
 */
@Component
@RequiredArgsConstructor
public class ClientLoginFailureHandler implements AuthenticationFailureHandler {

    private final JsonMapper jsonMapper;
    private final CompositeClientLoginFailureCallback failureCallback;
    private final List<ClientLoginProvider> loginProviders;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        String providerId = resolveProviderId(request);
        failureCallback.onLoginFailure(request, response, providerId, exception);

        Result<Void> body = resolveBody(exception);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), body);
    }

    private String resolveProviderId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (ClientLoginProvider provider : loginProviders) {
            if (uri != null && uri.endsWith("/login/" + provider.loginPathSegment())) {
                return provider.providerId();
            }
        }
        return "unknown";
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
