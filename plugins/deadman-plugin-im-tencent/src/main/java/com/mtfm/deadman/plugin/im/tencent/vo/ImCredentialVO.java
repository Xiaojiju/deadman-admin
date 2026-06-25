package com.mtfm.deadman.plugin.im.tencent.vo;

import java.time.Instant;

/**
 * IM 登录凭证，供前端初始化腾讯云 IM SDK。
 *
 * @param sdkAppId  腾讯云 IM SDKAppID
 * @param imUserId  腾讯云 UserID
 * @param userSig   UserSig
 * @param expireAt  UserSig 过期时间（UTC 秒级时间戳）
 */
public record ImCredentialVO(long sdkAppId, String imUserId, String userSig, long expireAt) {

    /**
     * 构造凭证并计算过期时间戳。
     *
     * @param sdkAppId          SDKAppID
     * @param imUserId          IM UserID
     * @param userSig           UserSig
     * @param expireSeconds     有效期（秒）
     * @return 凭证
     */
    public static ImCredentialVO of(long sdkAppId, String imUserId, String userSig, long expireSeconds) {
        long expireAt = Instant.now().getEpochSecond() + expireSeconds;
        return new ImCredentialVO(sdkAppId, imUserId, userSig, expireAt);
    }
}
