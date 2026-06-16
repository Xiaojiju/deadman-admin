package com.mtfm.deadman.component.client.auth.handler;

import com.mtfm.deadman.component.client.auth.ClientAuthenticatedUser;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.security.service.AuthTokenService;
import com.mtfm.deadman.security.spi.PendingOAuthBindLoginHandler;
import com.mtfm.deadman.security.support.AuthTokenLoginSuccessSupport;
import com.mtfm.deadman.security.token.AuthTokenSubject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;

/**
 * 用户端登录成功后签发双令牌并返回统一 JSON。
 */
@Component
public class ClientLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthTokenService authTokenService;
    private final ClientUserService clientUserService;
    private final JsonMapper jsonMapper;
    private final List<PendingOAuthBindLoginHandler> pendingOAuthBindLoginHandlers;

    /**
     * 构造登录成功处理器，聚合可选的待绑定 OAuth 处理器。
     *
     * @param authTokenService              JWT 签发服务
     * @param clientUserService             用户端用户服务
     * @param jsonMapper                    JSON 映射器
     * @param pendingOAuthBindLoginHandlers 待绑定 OAuth 处理器列表，无实现时为空列表
     */
    public ClientLoginSuccessHandler(
            AuthTokenService authTokenService,
            ClientUserService clientUserService,
            JsonMapper jsonMapper,
            List<PendingOAuthBindLoginHandler> pendingOAuthBindLoginHandlers) {
        this.authTokenService = authTokenService;
        this.clientUserService = clientUserService;
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

        ClientAuthenticatedUser loginUser = (ClientAuthenticatedUser) authentication.getPrincipal();
        ClientUserBase userBase = clientUserService.getById(loginUser.getUserId());
        AuthTokenLoginSuccessSupport.issueAndWrite(
                response,
                jsonMapper,
                authTokenService,
                ClientAuthConstants.JWT_REALM,
                new AuthTokenSubject(userBase.getId(), userBase.getUserCode(), userBase.getNickname()));
    }
}
