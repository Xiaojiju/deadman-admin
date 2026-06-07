package com.mtfm.deadman.component.client.service;

import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.spi.ClientUserProvisioner;
import com.mtfm.deadman.component.client.util.ClientUserCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 默认用户端用户注入实现，OAuth 插件可复用或覆盖。
 */
@Service
@RequiredArgsConstructor
public class DefaultClientUserProvisioner implements ClientUserProvisioner {

    private final ClientUserService clientUserService;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientComponentProperties clientComponentProperties;

    /**
     * 按 OAuth 信息创建或关联用户。
     *
     * @param request 注入请求
     * @return 用户基础信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClientUserBase provisionOAuthUser(ClientUserProvisionRequest request) {
        ClientUserAccount existing = clientUserAccountService.findByOAuth(request.provider(), request.subject());
        if (existing != null) {
            return clientUserService.requireById(existing.getUserId());
        }

        String userCode = ClientUserCodeGenerator.generate(clientComponentProperties.getUser().getUserCodePrefix());
        ClientUserBase userBase = ClientUserBase.builder()
                .userCode(userCode)
                .nickname(StringUtils.hasText(request.nickname()) ? request.nickname() : request.provider() + "用户")
                .avatar(request.avatar())
                .status(UserStatus.ACTIVE.getValue())
                .build();
        clientUserService.save(userBase);

        ClientUserAccount account = ClientUserAccount.builder()
                .userId(userBase.getId())
                .accountType(AccountType.OAUTH.getCode())
                .accountIdentifier(request.accountIdentifier())
                .oauthProvider(request.provider())
                .oauthSubject(request.subject())
                .verified(1)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        clientUserAccountService.save(account);
        return userBase;
    }
}
