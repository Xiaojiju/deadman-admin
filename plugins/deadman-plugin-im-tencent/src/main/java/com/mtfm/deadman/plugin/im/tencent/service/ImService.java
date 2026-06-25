package com.mtfm.deadman.plugin.im.tencent.service;

import java.time.LocalDateTime;
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
import com.mtfm.deadman.plugin.im.tencent.util.ImUserIdFormatter;
import com.mtfm.deadman.plugin.im.tencent.vo.ImCredentialVO;
import com.mtfm.deadman.plugin.im.tencent.vo.ImUserLookupVO;

import lombok.RequiredArgsConstructor;

/**
 * IM 编排服务：账号映射、资料同步与 UserSig 签发。
 */
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
        if (account == null) {
            account = ImUserAccount.builder()
                    .realmId(subject.realmId())
                    .subjectId(subject.subjectId())
                    .imUserId(imUserId)
                    .nickname(profile.nickname())
                    .avatarUrl(profile.avatarUrl())
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
        if (!needsSync(account, profile)) {
            return;
        }
        tencentImApiGateway.importAccount(account.getImUserId(), profile.nickname(), profile.avatarUrl());
        ImUserAccount update = ImUserAccount.builder()
                .id(account.getId())
                .nickname(profile.nickname())
                .avatarUrl(profile.avatarUrl())
                .status(ImUserAccountStatus.ACTIVE)
                .lastSyncTime(LocalDateTime.now())
                .build();
        imUserAccountMapper.updateById(update);
    }

    private boolean needsSync(ImUserAccount account, ImUserProfileSource profile) {
        if (account.getLastSyncTime() == null) {
            return true;
        }
        return !Objects.equals(account.getNickname(), profile.nickname())
                || !Objects.equals(account.getAvatarUrl(), profile.avatarUrl());
    }

    private static void validateSubject(ImSubject subject) {
        if (subject == null || !StringUtils.hasText(subject.realmId()) || !StringUtils.hasText(subject.subjectId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "IM 用户主体不完整");
        }
    }
}
