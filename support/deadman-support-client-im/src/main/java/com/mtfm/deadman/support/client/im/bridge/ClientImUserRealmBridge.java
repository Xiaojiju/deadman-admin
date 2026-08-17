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
import com.mtfm.deadman.plugin.file.service.FileService;
import com.mtfm.deadman.plugin.im.tencent.spi.ImSubject;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserProfileSource;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserRealmBridge;
import com.mtfm.deadman.plugin.im.tencent.util.ImFaceUrlSupport;
import com.mtfm.deadman.support.client.im.constant.ClientImRealmConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户端 IM 用户域桥接：client 用户体系 + file 换链后传入 IM（插件不直接依赖 FileService）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientImUserRealmBridge implements ImUserRealmBridge {

    private final ClientUserService clientUserService;
    private final FileService fileService;

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
        Long avatarFileId = profile.avatarFileId();
        String faceUrl = resolveFaceUrl(avatarFileId);
        return new ImUserProfileSource(profile.nickname(), faceUrl, avatarFileId, enabled);
    }

    /**
     * 通过 file 插件换链，仅将公网绝对 URL 交给 IM。
     * <p>微信会话 CDN 外链未写入 avatarFileId 时不在此伪造；须先上传平台文件再绑定 fileId。
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
            log.warn("用户端 IM 头像换链失败：avatarFileId={}", avatarFileId, ex);
            return null;
        }
        String faceUrl = ImFaceUrlSupport.normalizeOrNull(accessUrl);
        if (faceUrl == null) {
            log.warn("用户端 IM 头像非公网绝对 URL，已跳过 FaceUrl：avatarFileId={}, accessUrl={}",
                    avatarFileId, accessUrl);
        }
        return faceUrl;
    }
}
