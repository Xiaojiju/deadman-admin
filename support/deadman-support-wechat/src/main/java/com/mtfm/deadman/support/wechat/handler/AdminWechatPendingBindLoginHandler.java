package com.mtfm.deadman.support.wechat.handler;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.security.spi.PendingOAuthBindLoginHandler;
import com.mtfm.deadman.support.wechat.auth.AdminWechatPendingBindAuthenticationToken;
import com.mtfm.deadman.support.wechat.auth.AdminWechatPendingBindPrincipal;
import com.mtfm.deadman.support.wechat.vo.AdminWechatPendingBindVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 管理端微信 OAuth 待绑定登录成功响应处理器。
 */
@Component
@RequiredArgsConstructor
public class AdminWechatPendingBindLoginHandler implements PendingOAuthBindLoginHandler {

    private final JsonMapper jsonMapper;

    @Override
    public boolean supports(Authentication authentication) {
        return authentication instanceof AdminWechatPendingBindAuthenticationToken;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        AdminWechatPendingBindPrincipal principal = ((AdminWechatPendingBindAuthenticationToken) authentication)
                .getPrincipal();
        AdminWechatPendingBindVO body = new AdminWechatPendingBindVO(principal.bindToken(), principal.expiresIn(),
                true);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), Result.ok(body));
    }
}
