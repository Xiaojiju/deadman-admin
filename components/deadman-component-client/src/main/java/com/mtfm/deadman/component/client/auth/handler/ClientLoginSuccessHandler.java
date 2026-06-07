package com.mtfm.deadman.component.client.auth.handler;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.auth.ClientAuthenticatedUser;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.service.ClientAuthTokenService;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.component.client.vo.ClientAuthTokenVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 用户端登录成功后签发 JWT 并返回统一 JSON。
 */
@Component
@RequiredArgsConstructor
public class ClientLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ClientAuthTokenService clientAuthTokenService;
    private final ClientUserService clientUserService;
    private final JsonMapper jsonMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        ClientAuthenticatedUser loginUser = (ClientAuthenticatedUser) authentication.getPrincipal();
        ClientUserBase userBase = clientUserService.getById(loginUser.getUserId());
        ClientAuthTokenVO token = clientAuthTokenService.issueToken(userBase);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), Result.ok(token));
    }
}
