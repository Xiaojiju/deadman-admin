package com.mtfm.deadman.component.openauth.service;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.openauth.constant.OpenAuthConstants;
import com.mtfm.deadman.component.openauth.dto.OpenOAuthTokenRequest;
import com.mtfm.deadman.component.openauth.entity.OpenApp;
import com.mtfm.deadman.component.openauth.manager.OpenAuthSpiRegistry;
import com.mtfm.deadman.component.openauth.spi.OpenAuthScope;
import com.mtfm.deadman.component.openauth.spi.OpenAuthSubject;
import com.mtfm.deadman.component.openauth.store.AuthCodePayload;
import com.mtfm.deadman.component.openauth.store.AuthCodeStore;
import com.mtfm.deadman.component.openauth.token.OpenTokenIssueContext;
import com.mtfm.deadman.component.openauth.token.OpenTokenIssueResult;
import com.mtfm.deadman.component.openauth.token.OpenTokenIssuer;
import com.mtfm.deadman.component.openauth.util.OpenAuthCredentialGenerator;
import com.mtfm.deadman.component.openauth.util.OpenAuthTextSupport;
import com.mtfm.deadman.component.openauth.vo.OpenAuthCodeVO;
import com.mtfm.deadman.component.openauth.vo.OpenAuthSubjectVO;
import com.mtfm.deadman.component.openauth.vo.OpenOAuthTokenVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 开放授权门面服务，编排授权码签发与 token 兑换。
 */
@Service
@RequiredArgsConstructor
public class OpenAuthFacadeService {

    private final OpenAppAdminService openAppAdminService;
    private final OpenAuthSpiRegistry openAuthSpiRegistry;
    private final AuthCodeStore authCodeStore;
    private final OpenTokenIssuer openTokenIssuer;

    /**
     * 为已登录用户签发授权码。
     *
     * @param realm          用户域
     * @param authentication 当前认证对象
     * @param appId          目标应用 AppId
     * @return 授权码
     */
    public OpenAuthCodeVO issueAuthCode(String realm, Authentication authentication, String appId) {
        OpenApp app = openAppAdminService.requireEnabledByAppId(appId);
        validateRealm(app, realm);
        OpenAuthSubject subject = openAuthSpiRegistry.resolveSubject(realm, authentication);
        OpenAuthScope scope = openAuthSpiRegistry.requireScopeResolver(realm).resolve(subject, app);
        String code = OpenAuthCredentialGenerator.generateAuthCode();
        long ttlSeconds = app.getCodeTtlSec() == null ? 300 : app.getCodeTtlSec();
        authCodeStore.save(code, AuthCodePayload.of(app.getAppId(), realm, subject, scope), ttlSeconds);
        return new OpenAuthCodeVO(code, (int) ttlSeconds, app.getAppId());
    }

    /**
     * 使用 client 凭证与授权码兑换 open_access_token。
     *
     * @param request 兑换请求
     * @return access_token
     */
    public OpenOAuthTokenVO exchangeToken(OpenOAuthTokenRequest request) {
        if (!OpenAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE.equals(request.grantType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的 grant_type");
        }
        OpenApp app = openAppAdminService.requireEnabledByAppId(request.clientId());
        if (!openAppAdminService.matchesSecret(app, request.clientSecret())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "client_secret 无效");
        }
        AuthCodePayload payload = authCodeStore
                .consume(request.code().trim())
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "授权码无效或已过期"));
        if (!app.getAppId().equals(payload.appId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "授权码与应用不匹配");
        }
        long ttlSeconds = app.getTokenTtlSec() == null ? 3600 : app.getTokenTtlSec();
        OpenTokenIssueResult token = openTokenIssuer.issue(new OpenTokenIssueContext(
                app.getAppId(),
                payload.realm(),
                payload.subject().subjectType(),
                payload.subject().subjectId(),
                payload.subject().subjectCode(),
                payload.permissions(),
                payload.extensions(),
                ttlSeconds));
        return new OpenOAuthTokenVO(
                token.accessToken(),
                "Bearer",
                token.expiresIn(),
                token.scope(),
                new OpenAuthSubjectVO(
                        payload.realm(),
                        payload.subject().subjectType(),
                        payload.subject().subjectId(),
                        payload.subject().subjectCode()));
    }

    private void validateRealm(OpenApp app, String realm) {
        if (!StringUtils.hasText(realm)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开放授权用户域不能为空");
        }
        openAuthSpiRegistry.requireRealm(realm);
        if (!OpenAuthTextSupport.splitCommaValues(app.getAllowedRealms()).contains(realm)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该应用未授权访问用户域: " + realm);
        }
    }
}
