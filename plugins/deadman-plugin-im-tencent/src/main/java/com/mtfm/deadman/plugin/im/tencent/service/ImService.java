package com.mtfm.deadman.plugin.im.tencent.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.im.tencent.client.TencentImApiGateway;
import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;
import com.mtfm.deadman.plugin.im.tencent.constant.ImUserAccountStatus;
import com.mtfm.deadman.plugin.im.tencent.entity.ImUserAccount;
import com.mtfm.deadman.plugin.im.tencent.manager.ImUserRealmBridgeRegistry;
import com.mtfm.deadman.plugin.im.tencent.mapper.ImUserAccountMapper;
import com.mtfm.deadman.plugin.im.tencent.spi.ImSubject;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserProfileSource;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserRealmBridge;
import com.mtfm.deadman.plugin.im.tencent.util.ImFaceUrlSupport;
import com.mtfm.deadman.plugin.im.tencent.util.ImUserIdFormatter;
import com.mtfm.deadman.plugin.im.tencent.vo.ImCredentialVO;
import com.mtfm.deadman.plugin.im.tencent.vo.ImUserLookupVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IM 编排服务：账号映射、资料同步与 UserSig 签发。
 * <p>不依赖文件插件；头像 URL 由 Support 调用方换链后通过 {@link ImUserProfileSource} 传入。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImService {

    private final ImTencentPluginProperties properties;
    private final ImUserRealmBridgeRegistry bridgeRegistry;
    private final ImUserAccountMapper imUserAccountMapper;
    private final ImUserIdFormatter imUserIdFormatter;
    private final TencentImApiGateway tencentImApiGateway;

    /**
     * 为指定主体签发 IM 登录凭证。
     *
     * @param subject 抽象用户主体
     * @return 登录凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public ImCredentialVO issueCredential(ImSubject subject) {
        validateSubject(subject);
        properties.requireProductionConfig();
        ImUserRealmBridge bridge = bridgeRegistry.require(subject.realmId());
        ImUserProfileSource profile = bridge.resolveProfileSource(subject);
        if (!profile.enabled()) {
            throw new BusinessException(ResultCode.IM_USER_DISABLED);
        }
        ImUserAccount account = ensureAccount(subject, profile);
        syncIfNeeded(account, profile);
        String userSig = tencentImApiGateway.generateUserSig(account.getImUserId());
        long sdkAppId = properties.getSdkAppId() == null ? 0L : properties.getSdkAppId();
        return ImCredentialVO.of(sdkAppId, account.getImUserId(), userSig, properties.getUserSigExpireSeconds());
    }

    /**
     * 为指定用户域的当前登录用户签发 IM 登录凭证。
     *
     * @param realmId 用户域标识
     * @return 登录凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public ImCredentialVO issueCredentialForCurrentUser(String realmId) {
        ImUserRealmBridge bridge = bridgeRegistry.require(realmId);
        ImSubject subject = bridge.resolveCurrentSubject()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录或无法解析 IM 用户主体"));
        return issueCredential(subject);
    }

    /**
     * 主动同步用户资料到腾讯云 IM。
     *
     * @param subject 抽象用户主体
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncUserProfile(ImSubject subject) {
        validateSubject(subject);
        ImUserRealmBridge bridge = bridgeRegistry.require(subject.realmId());
        ImUserProfileSource profile = bridge.resolveProfileSource(subject);
        if (!profile.enabled()) {
            throw new BusinessException(ResultCode.IM_USER_DISABLED);
        }
        ImUserAccount account = ensureAccount(subject, profile);
        syncIfNeeded(account, profile);
    }

    /**
     * 按业务主体查询 IM UserID 映射。
     *
     * @param realmId   用户域标识
     * @param subjectId 域内主键
     * @return 映射结果
     */
    @Transactional(readOnly = true)
    public ImUserLookupVO lookupImUser(String realmId, String subjectId) {
        validateSubject(new ImSubject(realmId, subjectId));
        ImUserAccount account = imUserAccountMapper.selectOne(new LambdaQueryWrapper<ImUserAccount>()
                .eq(ImUserAccount::getRealmId, realmId)
                .eq(ImUserAccount::getSubjectId, subjectId));
        if (account == null) {
            throw new BusinessException(ResultCode.IM_USER_NOT_FOUND);
        }
        return new ImUserLookupVO(realmId, subjectId, account.getImUserId());
    }

    private ImUserAccount ensureAccount(ImSubject subject, ImUserProfileSource profile) {
        ImUserAccount account = imUserAccountMapper.selectOne(new LambdaQueryWrapper<ImUserAccount>()
                .eq(ImUserAccount::getRealmId, subject.realmId())
                .eq(ImUserAccount::getSubjectId, subject.subjectId()));
        String imUserId = imUserIdFormatter.format(subject.realmId(), subject.subjectId());
        String faceUrl = ImFaceUrlSupport.normalizeOrNull(profile.avatarUrl());
        if (account == null) {
            account = ImUserAccount.builder()
                    .realmId(subject.realmId())
                    .subjectId(subject.subjectId())
                    .imUserId(imUserId)
                    .nickname(profile.nickname())
                    .avatarFileId(profile.avatarFileId())
                    .avatarUrl(faceUrl)
                    .status(ImUserAccountStatus.ACTIVE)
                    .build();
            imUserAccountMapper.insert(account);
            return account;
        }
        if (!Objects.equals(account.getImUserId(), imUserId)) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "IM UserID 映射冲突，请检查 user-id-template 配置");
        }
        return account;
    }

    private void syncIfNeeded(ImUserAccount account, ImUserProfileSource profile) {
        String faceUrl = ImFaceUrlSupport.normalizeOrNull(profile.avatarUrl());
        if (!needsSync(account, profile, faceUrl)) {
            return;
        }
        if (profile.avatarFileId() != null && faceUrl == null) {
            log.warn("IM 头像 fileId={} 未解析为公网绝对 URL，跳过 FaceUrl 写入：imUserId={}",
                    profile.avatarFileId(), account.getImUserId());
        }
        tencentImApiGateway.importAccount(account.getImUserId(), profile.nickname(), faceUrl);
        ImUserAccount update = ImUserAccount.builder()
                .id(account.getId())
                .nickname(profile.nickname())
                .avatarFileId(profile.avatarFileId())
                .avatarUrl(faceUrl)
                .status(ImUserAccountStatus.ACTIVE)
                .lastSyncTime(LocalDateTime.now())
                .build();
        imUserAccountMapper.updateById(update);
        account.setNickname(profile.nickname());
        account.setAvatarFileId(profile.avatarFileId());
        account.setAvatarUrl(faceUrl);
        account.setLastSyncTime(update.getLastSyncTime());
    }

    private boolean needsSync(ImUserAccount account, ImUserProfileSource profile, String faceUrl) {
        if (account.getLastSyncTime() == null) {
            return true;
        }
        if (!Objects.equals(account.getNickname(), profile.nickname())) {
            return true;
        }
        // 以 fileId 为稳定键，避免签名 URL 每次变化导致无意义比对失败
        if (!Objects.equals(account.getAvatarFileId(), profile.avatarFileId())) {
            return true;
        }
        // 历史快照非公网地址，现已有可用 FaceUrl → 补推
        if (faceUrl != null && !ImFaceUrlSupport.isAbsoluteHttpUrl(account.getAvatarUrl())) {
            return true;
        }
        // 签名链过期前按间隔刷新 FaceUrl
        if (faceUrl != null && isFaceUrlRefreshDue(account.getLastSyncTime())) {
            return true;
        }
        return false;
    }

    private boolean isFaceUrlRefreshDue(LocalDateTime lastSyncTime) {
        long refreshSeconds = properties.getFaceUrlRefreshSeconds();
        if (refreshSeconds <= 0) {
            return false;
        }
        long elapsed = ChronoUnit.SECONDS.between(lastSyncTime, LocalDateTime.now());
        return elapsed >= refreshSeconds;
    }

    private static void validateSubject(ImSubject subject) {
        if (subject == null || !StringUtils.hasText(subject.realmId()) || !StringUtils.hasText(subject.subjectId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "IM 用户主体不完整");
        }
    }
}
