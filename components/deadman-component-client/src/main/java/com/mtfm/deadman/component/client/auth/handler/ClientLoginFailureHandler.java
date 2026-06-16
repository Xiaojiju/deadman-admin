package com.mtfm.deadman.component.client.auth.handler;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroupManager;
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
 * 用户端登录失败处理器，统一 JSON 响应并触发失败回调 SPI。
 */
@Component
@RequiredArgsConstructor
public class ClientLoginFailureHandler implements AuthenticationFailureHandler {

    private final JsonMapper jsonMapper;
    private final CompositeClientLoginFailureCallback failureCallback;
    private final LoginProviderGroupManager loginProviderGroupManager;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        String providerId = loginProviderGroupManager.resolveProviderIdByLoginUri(
                ClientAuthConstants.LOGIN_GROUP_ID, request.getRequestURI());
        failureCallback.onLoginFailure(request, response, providerId, exception);
        LoginFailureResponseSupport.writeLoginFailure(response, jsonMapper, exception);
    }
}
