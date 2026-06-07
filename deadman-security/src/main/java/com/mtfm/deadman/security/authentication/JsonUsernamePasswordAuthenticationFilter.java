package com.mtfm.deadman.security.authentication;

import com.mtfm.deadman.security.dto.auth.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 扩展 {@link UsernamePasswordAuthenticationFilter}，从 JSON 请求体读取用户名密码。
 */
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JsonMapper jsonMapper;

    public JsonUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, JsonMapper jsonMapper) {
        super(authenticationManager);
        this.jsonMapper = jsonMapper;
        setPostOnly(true);
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        LoginRequest loginRequest = parseLoginRequest(request);
        String username = loginRequest.username();
        String password = loginRequest.password();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new AuthenticationServiceException("用户名或密码不能为空");
        }

        UsernamePasswordAuthenticationToken authRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(username.trim(), password);
        setDetails(request, authRequest);
        return getAuthenticationManager().authenticate(authRequest);
    }

    private LoginRequest parseLoginRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("登录请求解析失败", ex);
        }
    }
}
