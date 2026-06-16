package com.mtfm.deadman.security.authentication;

import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.service.AuthTokenService;
import com.mtfm.deadman.security.spi.PendingOAuthBindLoginHandler;
import com.mtfm.deadman.security.support.AuthTokenLoginSuccessSupport;
import com.mtfm.deadman.security.token.AuthTokenSubject;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;

/**
 * 登录成功后签发 JWT 并返回统一 JSON。
 */
@Component
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthTokenService authTokenService;
    private final UserService userService;
    private final JsonMapper jsonMapper;
    private final List<PendingOAuthBindLoginHandler> pendingOAuthBindLoginHandlers;

    /**
     * 构造登录成功处理器，聚合可选的待绑定 OAuth 处理器。
     *
     * @param authTokenService              JWT 签发服务
     * @param userService                   用户服务
     * @param jsonMapper                    JSON 映射器
     * @param pendingOAuthBindLoginHandlers 待绑定 OAuth 处理器列表，无实现时为空列表
     */
    public JwtLoginSuccessHandler(
            AuthTokenService authTokenService,
            UserService userService,
            JsonMapper jsonMapper,
            List<PendingOAuthBindLoginHandler> pendingOAuthBindLoginHandlers) {
        this.authTokenService = authTokenService;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.pendingOAuthBindLoginHandlers = pendingOAuthBindLoginHandlers == null ? List.of()
                : pendingOAuthBindLoginHandlers;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        for (PendingOAuthBindLoginHandler handler : pendingOAuthBindLoginHandlers) {
            if (handler.supports(authentication)) {
                handler.onAuthenticationSuccess(request, response, authentication);
                return;
            }
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        UserBase userBase = userService.getById(loginUser.getUserId());
        AuthTokenLoginSuccessSupport.issueAndWrite(
                response,
                jsonMapper,
                authTokenService,
                AdminAuthConstants.JWT_REALM,
                new AuthTokenSubject(userBase.getId(), userBase.getUserCode(), userBase.getNickname()));
    }
}
