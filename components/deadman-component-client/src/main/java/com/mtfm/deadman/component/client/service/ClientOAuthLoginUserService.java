package com.mtfm.deadman.component.client.service;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.spi.ClientUserProvisioner;
import com.mtfm.deadman.security.spi.OAuthLoginUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * 用户端 OAuth 登录用户解析服务，供微信等 OAuth 插件按 loginGroupId 路由注入用户。
 */
@Service
@RequiredArgsConstructor
public class ClientOAuthLoginUserService implements OAuthLoginUserService {

    private final ClientUserAccountService clientUserAccountService;
    private final ClientUserService clientUserService;
    private final ClientUserProvisioner clientUserProvisioner;

    @Override
    public String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    @Override
    public Authentication resolveOAuthLogin(OAuthLoginRequest request) throws AuthenticationException {
        ClientUserAccount account =
                clientUserAccountService.findByOAuth(request.oauthProvider(), request.oauthSubject());
        ClientUserBase userBase;
        if (account != null) {
            userBase = clientUserService.requireById(account.getUserId());
        } else {
            userBase = clientUserProvisioner.provisionOAuthUser(new ClientUserProvisioner.ClientUserProvisionRequest(
                    request.oauthProvider(),
                    request.oauthSubject(),
                    request.loginIdentifier(),
                    request.nickname(),
                    request.avatar()));
        }

        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException("用户已禁用");
        }

        ClientLoginUser loginUser = clientUserService.buildLoginUser(userBase, request.loginIdentifier());
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
    }
}
