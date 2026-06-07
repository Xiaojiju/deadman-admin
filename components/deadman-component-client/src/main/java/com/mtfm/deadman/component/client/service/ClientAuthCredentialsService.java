package com.mtfm.deadman.component.client.service;

import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.dto.ClientRegisterRequest;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.util.ClientUserCodeGenerator;
import com.mtfm.deadman.component.client.vo.ClientAuthTokenVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户端认证凭证：注册与令牌签发。
 */
@Service
@RequiredArgsConstructor
public class ClientAuthCredentialsService {

    private final ClientUserService clientUserService;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientUserPasswordService clientUserPasswordService;
    private final ClientAuthTokenService clientAuthTokenService;
    private final ClientComponentProperties clientComponentProperties;

    /**
     * 用户注册并签发 JWT。
     *
     * @param request 注册请求
     * @return 访问令牌
     */
    @Transactional(rollbackFor = Exception.class)
    public ClientAuthTokenVO register(ClientRegisterRequest request) {
        if (clientUserAccountService.existsUsername(request.username())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }

        String userCode = ClientUserCodeGenerator.generate(clientComponentProperties.getUser().getUserCodePrefix());
        ClientUserBase userBase = ClientUserBase.builder()
                .userCode(userCode)
                .nickname(request.nickname() != null ? request.nickname() : request.username())
                .status(UserStatus.ACTIVE.getValue())
                .build();
        clientUserService.save(userBase);

        ClientUserAccount account = ClientUserAccount.builder()
                .userId(userBase.getId())
                .accountType(AccountType.USERNAME.getCode())
                .accountIdentifier(request.username())
                .verified(1)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        clientUserAccountService.save(account);

        clientUserPasswordService.createPassword(userBase.getId(), request.password());
        return clientAuthTokenService.issueToken(userBase);
    }
}
