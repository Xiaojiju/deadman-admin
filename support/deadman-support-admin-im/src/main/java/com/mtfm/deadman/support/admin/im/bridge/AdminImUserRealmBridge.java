package com.mtfm.deadman.support.admin.im.bridge;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.file.service.FileService;
import com.mtfm.deadman.plugin.im.tencent.spi.ImSubject;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserProfileSource;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserRealmBridge;
import com.mtfm.deadman.plugin.im.tencent.util.ImFaceUrlSupport;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.support.admin.im.constant.AdminImRealmConstants;
import com.mtfm.deadman.system.service.UserService;
import com.mtfm.deadman.system.vo.user.UserProfileVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理端 IM 用户域桥接：system 用户体系 + file 换链后传入 IM（插件不直接依赖 FileService）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminImUserRealmBridge implements ImUserRealmBridge {

    private final UserService userService;
    private final FileService fileService;

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
        Long avatarFileId = profile.getAvatarFileId();
        String faceUrl = resolveFaceUrl(avatarFileId);
        return new ImUserProfileSource(profile.getNickname(), faceUrl, avatarFileId, enabled);
    }

    /**
     * 通过 file 插件换链，仅将公网绝对 URL 交给 IM。
     *
     * @param avatarFileId 头像文件 ID
     * @return FaceUrl，不可用时为 null
     */
    private String resolveFaceUrl(Long avatarFileId) {
        if (avatarFileId == null) {
            return null;
        }
        String accessUrl;
        try {
            accessUrl = fileService.resolveAccessUrl(avatarFileId).accessUrl();
        } catch (RuntimeException ex) {
            log.warn("管理端 IM 头像换链失败：avatarFileId={}", avatarFileId, ex);
            return null;
        }
        String faceUrl = ImFaceUrlSupport.normalizeOrNull(accessUrl);
        if (faceUrl == null) {
            log.warn("管理端 IM 头像非公网绝对 URL，已跳过 FaceUrl：avatarFileId={}, accessUrl={}",
                    avatarFileId, accessUrl);
        }
        return faceUrl;
    }
}
