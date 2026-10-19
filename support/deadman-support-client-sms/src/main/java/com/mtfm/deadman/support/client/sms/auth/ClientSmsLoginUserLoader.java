package com.mtfm.deadman.support.client.sms.auth;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.component.client.util.ClientUserCodeGenerator;
import com.mtfm.deadman.plugin.smslogin.SmsLoginMessages;
import com.mtfm.deadman.plugin.smslogin.auth.SmsCodeAuthenticationToken;
import com.mtfm.deadman.plugin.smslogin.spi.SmsLoginUserLoader;

import lombok.RequiredArgsConstructor;

/**
 * 用户端短信登录：验证码已由插件消费，这里按手机号查找或自动开通账号。
 */
@Service
@RequiredArgsConstructor
public class ClientSmsLoginUserLoader implements SmsLoginUserLoader {

    private final ClientUserService clientUserService;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientComponentProperties clientComponentProperties;

    /**
     * 归属用户端登录组。
     *
     * @return client
     */
    @Override
    public String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    /**
     * 查找或开通用户端账号，并返回已认证令牌。
     *
     * @param phone 已规范化的手机号
     * @return 已认证令牌
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Authentication loadAuthenticatedUser(String phone) {
        ClientUserAccount account = clientUserAccountService.findByPhone(phone);
        ClientUserBase user = account == null ? provision(phone) : clientUserService.requireById(account.getUserId());
        if (user.getStatus() == null || user.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException(SmsLoginMessages.text("sms.login.user_disabled", "用户已禁用"));
        }
        ClientLoginUser loginUser = clientUserService.buildLoginUser(user, phone);
        return new SmsCodeAuthenticationToken(loginUser, loginUser.getAuthorities());
    }

    private ClientUserBase provision(String phone) {
        String prefix = clientComponentProperties.getUser().getUserCodePrefix();
        ClientUserBase user = ClientUserBase.builder().userCode(ClientUserCodeGenerator.generate(prefix))
            .nickname("用户" + phone.substring(phone.length() - 4)).status(UserStatus.ACTIVE.getValue()).build();
        clientUserService.save(user);
        clientUserAccountService.bindOrUpdatePhone(user.getId(), phone);
        return user;
    }
}
