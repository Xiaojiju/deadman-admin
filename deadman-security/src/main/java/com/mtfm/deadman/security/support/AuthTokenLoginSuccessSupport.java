package com.mtfm.deadman.security.support;

import com.mtfm.deadman.security.service.AuthTokenService;
import com.mtfm.deadman.security.token.AuthTokenSubject;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 登录成功后签发双令牌并写回 JSON 响应。
 */
public final class AuthTokenLoginSuccessSupport {

    private AuthTokenLoginSuccessSupport() {
    }

    /**
     * 按端签发 Access/Refresh Token 并写入响应（含 HttpOnly Cookie）。
     *
     * @param response         HTTP 响应
     * @param jsonMapper       JSON 映射器
     * @param authTokenService 令牌门面
     * @param realm            端标识
     * @param subject          令牌主体
     * @return 签发的双令牌视图
     */
    public static AuthTokenVO issueAndWrite(
            HttpServletResponse response,
            JsonMapper jsonMapper,
            AuthTokenService authTokenService,
            String realm,
            AuthTokenSubject subject)
            throws IOException {
        var provider = authTokenService.requireProvider(realm);
        AuthTokenVO token = authTokenService.issue(realm, subject);
        AuthTokenResponseSupport.writeTokenSuccess(response, jsonMapper, token, provider);
        return token;
    }
}
