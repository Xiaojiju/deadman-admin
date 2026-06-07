package com.mtfm.deadman.plugin.wechat.miniprogram.client;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * 微信 access_token 内存缓存（提前 120 秒过期）。
 */
@Component
public class WechatAccessTokenHolder {

    private static final long REFRESH_BUFFER_SECONDS = 120L;

    private volatile String accessToken;
    private volatile Instant expiresAt = Instant.EPOCH;
    private volatile long configuredExpiresInSeconds = 7200L;

    /**
     * 获取有效 access_token，过期时自动刷新。
     *
     * @param tokenSupplier 刷新函数
     * @return access_token
     */
    public synchronized String getAccessToken(Supplier<String> tokenSupplier) {
        if (accessToken != null && Instant.now().isBefore(expiresAt)) {
            return accessToken;
        }
        accessToken = tokenSupplier.get();
        long ttl = Math.max(configuredExpiresInSeconds - REFRESH_BUFFER_SECONDS, 60L);
        expiresAt = Instant.now().plusSeconds(ttl);
        return accessToken;
    }

    /**
     * 更新微信返回的过期秒数。
     *
     * @param expiresInSeconds 过期秒数
     */
    public void updateExpiresIn(long expiresInSeconds) {
        this.configuredExpiresInSeconds = expiresInSeconds;
    }
}
