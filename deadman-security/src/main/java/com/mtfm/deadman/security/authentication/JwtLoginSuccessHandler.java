package com.mtfm.deadman.security.authentication;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.security.service.AuthTokenService;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserService;
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
 * 登录成功后签发 JWT 并返回统一 JSON。
 */
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthTokenService authTokenService;
    private final UserService userService;
    private final JsonMapper jsonMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        UserBase userBase = userService.getById(loginUser.getUserId());
        AuthTokenVO token = authTokenService.issueToken(userBase);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), Result.ok(token));
    }
}
