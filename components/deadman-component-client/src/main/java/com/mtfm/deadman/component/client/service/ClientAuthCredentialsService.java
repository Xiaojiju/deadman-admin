package com.mtfm.deadman.component.client.service;

import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.dto.ClientChangePasswordRequest;
import com.mtfm.deadman.component.client.dto.ClientRegisterRequest;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.event.ClientUserRegisteredEvent;
import com.mtfm.deadman.component.client.util.ClientUserCodeGenerator;
import com.mtfm.deadman.security.vo.auth.RegisterResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户端认证凭证：注册（令牌仅在登录成功后签发）。
 */
@Service
@RequiredArgsConstructor
public class ClientAuthCredentialsService {

    private final ClientUserService clientUserService;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientUserPasswordService clientUserPasswordService;
    private final ClientComponentProperties clientComponentProperties;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 用户端注册（不签发令牌，需登录后获取 Access/Refresh Token）。
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResultVO register(ClientRegisterRequest request) {
        ClientUserBase userBase = registerUser(request);
        return new RegisterResultVO(userBase.getUserCode(), userBase.getNickname());
    }

    /**
     * 用户端修改密码（校验原密码）。
     *
     * @param userId 用户 ID
     * @param request 原密码与新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ClientChangePasswordRequest request) {
        clientUserPasswordService.changePassword(userId, request.oldPassword(), request.newPassword());
    }

    /**
     * 创建用户端账号（用户名 + 密码 + 可选手机号），供注册与微信绑定注册复用。
     *
     * @param request 注册请求
     * @return 新用户基础信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ClientUserBase registerUser(ClientRegisterRequest request) {
        if (clientUserAccountService.existsUsername(request.username())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }

        String userCode = ClientUserCodeGenerator.generate(clientComponentProperties.getUser().getUserCodePrefix());
        ClientUserBase userBase = ClientUserBase.builder().userCode(userCode)
            .nickname(request.nickname() != null ? request.nickname() : request.username())
            .avatarFileId(request.avatarFileId())
            .status(UserStatus.ACTIVE.getValue()).build();
        clientUserService.save(userBase);

        ClientUserAccount account =
            ClientUserAccount.builder().userId(userBase.getId()).accountType(AccountType.USERNAME.getCode())
                .accountIdentifier(request.username()).verified(1).status(UserStatus.ACTIVE.getValue()).build();
        clientUserAccountService.save(account);

        // 普通注册必填手机号；微信绑定注册可暂不传，后续通过 getPhoneNumber 绑定
        if (StringUtils.hasText(request.phone())) {
            clientUserAccountService.bindOrUpdatePhone(userBase.getId(), request.phone().trim());
        }

        clientUserPasswordService.createPassword(userBase.getId(), request.password());
        eventPublisher.publishEvent(new ClientUserRegisteredEvent(userBase.getId(),
            StringUtils.hasText(request.inviteCode()) ? request.inviteCode().trim() : null));
        return userBase;
    }
}
