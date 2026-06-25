package com.mtfm.deadman.support.client.im.bridge;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.component.client.vo.ClientUserProfileVO;
import com.mtfm.deadman.plugin.im.tencent.spi.ImSubject;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserProfileSource;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserRealmBridge;
import com.mtfm.deadman.support.client.im.constant.ClientImRealmConstants;

import lombok.RequiredArgsConstructor;

/**
 * 用户端 IM 用户域桥接，将 client 用户体系映射为 IM 抽象主体。
 */
@Component
@RequiredArgsConstructor
public class ClientImUserRealmBridge implements ImUserRealmBridge {

    private final ClientUserService clientUserService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String realmId() {
        return ClientImRealmConstants.REALM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ImSubject> resolveCurrentSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ClientLoginUser loginUser) {
            return Optional.of(new ImSubject(ClientImRealmConstants.REALM_ID, loginUser.getUserCode()));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImUserProfileSource resolveProfileSource(ImSubject subject) {
        if (!ClientImRealmConstants.REALM_ID.equals(subject.realmId())) {
            throw new BusinessException(ResultCode.IM_REALM_UNKNOWN, "非 client 用户域：" + subject.realmId());
        }
        ClientUserProfileVO profile = clientUserService.getProfileByUserCode(subject.subjectId());
        boolean enabled = profile.status() != null && profile.status() == UserStatus.ACTIVE.getValue();
        return new ImUserProfileSource(profile.nickname(), profile.avatar(), enabled);
    }
}
