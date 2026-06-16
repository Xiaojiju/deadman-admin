package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.security.token.RealmJwtSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按端隔离的 Refresh Token 存储：每个用户仅保留一个当前有效的 Refresh jti，刷新时原子替换，并检测已轮换 jti 的重用。
 */
@Slf4j
public class RealmJwtRefreshTokenStore {

    private static final String USED_KEY_SEGMENT = "used:";

    private static final DefaultRedisScript<Long> ROTATE_REFRESH_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('GET', KEYS[1]) == ARGV[1] then
                      redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
                      redis.call('SET', KEYS[2], ARGV[4], 'PX', ARGV[3])
                      return 1
                    end
                    return 0
                    """,
            Long.class);

    private final RealmJwtSettings settings;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<Long, String> localActiveRefreshJti = new ConcurrentHashMap<>();
    private final Map<String, Long> localUsedRefreshJti = new ConcurrentHashMap<>();
    private final Map<Long, Object> localUserLocks = new ConcurrentHashMap<>();

    /**
     * @param settings              端 JWT 配置
     * @param redisTemplateProvider Redis 模板（可选）
     */
    public RealmJwtRefreshTokenStore(
            RealmJwtSettings settings, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.settings = settings;
        this.redisTemplateProvider = redisTemplateProvider;
    }

    /**
     * 注册当前有效 Refresh Token（登录或首次签发），覆盖该用户此前所有 Refresh Token。
     *
     * @param userId 用户主键
     * @param jti    Refresh Token 唯一标识
     */
    public void registerRefreshToken(Long userId, String jti) {
        Duration ttl = Duration.ofMillis(settings.refreshExpirationMs());
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(userRefreshKey(userId), jti, ttl);
            return;
        }
        localActiveRefreshJti.put(userId, jti);
        log.debug("Redis 不可用，realm={} 使用内存 Refresh Token 存储 userId={}", settings.realm(), userId);
    }

    /**
     * 校验 Refresh Token jti 的状态（当前有效 / 已重用 / 无效）。
     *
     * @param userId 用户主键
     * @param jti    Refresh Token 唯一标识
     * @return 校验结果
     */
    public RefreshTokenCheckResult checkRefreshToken(Long userId, String jti) {
        if (jti == null || jti.isBlank() || userId == null) {
            return RefreshTokenCheckResult.INVALID;
        }
        if (isRefreshTokenActive(userId, jti)) {
            return RefreshTokenCheckResult.ACTIVE;
        }
        if (isPreviouslyUsedRefreshJti(userId, jti)) {
            return RefreshTokenCheckResult.REUSED;
        }
        return RefreshTokenCheckResult.INVALID;
    }

    /**
     * 判断 Refresh Token jti 是否为该用户当前唯一有效 Refresh Token。
     *
     * @param userId 用户主键
     * @param jti    Refresh Token 唯一标识
     * @return 是否有效
     */
    public boolean isRefreshTokenActive(Long userId, String jti) {
        if (jti == null || jti.isBlank() || userId == null) {
            return false;
        }
        String activeJti = getActiveRefreshJti(userId);
        return jti.equals(activeJti);
    }

    /**
     * 刷新轮换：仅当 {@code expectedJti} 仍为当前有效 Refresh jti 时，原子替换为 {@code newJti}，并将旧 jti
     * 记入已使用集合。
     *
     * @param userId      用户主键
     * @param expectedJti 请求携带的 Refresh jti
     * @param newJti      新的 Refresh jti
     * @return 是否轮换成功
     */
    public boolean rotateRefreshToken(Long userId, String expectedJti, String newJti) {
        if (userId == null || expectedJti == null || expectedJti.isBlank() || newJti == null || newJti.isBlank()) {
            return false;
        }
        Duration ttl = Duration.ofMillis(settings.refreshExpirationMs());
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            Long rotated = redisTemplate.execute(
                    ROTATE_REFRESH_SCRIPT,
                    java.util.List.of(userRefreshKey(userId), usedRefreshKey(expectedJti)),
                    expectedJti,
                    newJti,
                    String.valueOf(settings.refreshExpirationMs()),
                    String.valueOf(userId));
            return rotated != null && rotated == 1L;
        }
        Object lock = localUserLocks.computeIfAbsent(userId, ignored -> new Object());
        synchronized (lock) {
            if (!expectedJti.equals(localActiveRefreshJti.get(userId))) {
                return false;
            }
            localActiveRefreshJti.put(userId, newJti);
            localUsedRefreshJti.put(expectedJti, userId);
            return true;
        }
    }

    /**
     * 撤销用户当前 Refresh Token。
     *
     * @param userId 用户主键
     */
    public void revokeAllForUser(Long userId) {
        if (userId == null) {
            return;
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.delete(userRefreshKey(userId));
            return;
        }
        String activeJti = localActiveRefreshJti.remove(userId);
        if (activeJti != null) {
            localUsedRefreshJti.remove(activeJti);
        }
    }

    private boolean isPreviouslyUsedRefreshJti(Long userId, String jti) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            String storedUserId = redisTemplate.opsForValue().get(usedRefreshKey(jti));
            return storedUserId != null && String.valueOf(userId).equals(storedUserId);
        }
        return userId.equals(localUsedRefreshJti.get(jti));
    }

    private String getActiveRefreshJti(Long userId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(userRefreshKey(userId));
        }
        return localActiveRefreshJti.get(userId);
    }

    private String userRefreshKey(Long userId) {
        return settings.refreshUserIndexPrefix() + userId;
    }

    private String usedRefreshKey(String jti) {
        return settings.refreshKeyPrefix() + USED_KEY_SEGMENT + jti;
    }
}
