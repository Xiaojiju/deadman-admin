package com.mtfm.deadman.support.admin.im.bridge;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.im.tencent.spi.ImSubject;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserProfileSource;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserRealmBridge;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.support.admin.im.constant.AdminImRealmConstants;
import com.mtfm.deadman.system.service.UserService;
import com.mtfm.deadman.system.vo.user.UserProfileVO;

import lombok.RequiredArgsConstructor;

/**
 * 管理端 IM 用户域桥接，将 system 用户体系映射为 IM 抽象主体。
 */
@Component
@RequiredArgsConstructor
public class AdminImUserRealmBridge implements ImUserRealmBridge {

    private final UserService userService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String realmId() {
        return AdminImRealmConstants.REALM_ID;
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
        if (principal instanceof LoginUser loginUser) {
            return Optional.of(new ImSubject(AdminImRealmConstants.REALM_ID, loginUser.getUserCode()));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImUserProfileSource resolveProfileSource(ImSubject subject) {
        if (!AdminImRealmConstants.REALM_ID.equals(subject.realmId())) {
            throw new BusinessException(ResultCode.IM_REALM_UNKNOWN, "非 admin 用户域：" + subject.realmId());
        }
        UserProfileVO profile = userService.getProfileByUserCode(subject.subjectId());
        boolean enabled = profile.getStatus() != null && profile.getStatus() == UserStatus.ACTIVE.getValue();
        return new ImUserProfileSource(profile.getNickname(), profile.getAvatar(), enabled);
    }
}
