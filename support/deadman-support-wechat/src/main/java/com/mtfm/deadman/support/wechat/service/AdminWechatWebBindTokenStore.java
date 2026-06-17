package com.mtfm.deadman.support.wechat.service;

import com.mtfm.deadman.common.constants.RedisKeyConstants;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.support.wechat.config.AdminWechatSupportProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.UUID;

/**
 * 管理端微信网页扫码 OAuth 待绑定临时令牌 Redis 存储。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-web", name = "enabled", havingValue = "true")
public class AdminWechatWebBindTokenStore {

    private final StringRedisTemplate stringRedisTemplate;
    private final AdminWechatSupportProperties properties;
    private final JsonMapper jsonMapper;

    /**
     * 保存待绑定会话并返回临时绑定令牌。
     *
     * @param session 网页授权用户信息
     * @return 临时绑定令牌
     */
    public String store(AdminWechatWebPendingSession session) {
        String bindToken = UUID.randomUUID().toString().replace("-", "");
        String key = RedisKeyConstants.adminWechatWebBindKey(bindToken);
        Duration ttl = properties.getBindTokenTtl();
        try {
            String payload = jsonMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(key, payload, ttl);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信网页绑定令牌写入失败");
        }
        return bindToken;
    }

    /**
     * 读取并删除待绑定会话（一次性消费）。
     *
     * @param bindToken 临时绑定令牌
     * @return 待绑定会话
     */
    public AdminWechatWebPendingSession consume(String bindToken) {
        if (!StringUtils.hasText(bindToken)) {
            throw new BusinessException(ResultCode.WECHAT_BIND_TOKEN_INVALID);
        }
        String key = RedisKeyConstants.adminWechatWebBindKey(bindToken.trim());
        String payload = stringRedisTemplate.opsForValue().getAndDelete(key);
        if (!StringUtils.hasText(payload)) {
            throw new BusinessException(ResultCode.WECHAT_BIND_TOKEN_INVALID);
        }
        try {
            return jsonMapper.readValue(payload, AdminWechatWebPendingSession.class);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.WECHAT_BIND_TOKEN_INVALID);
        }
    }

    /**
     * 获取绑定令牌有效秒数。
     *
     * @return TTL 秒数
     */
    public long bindTokenExpiresInSeconds() {
        return properties.getBindTokenTtl().toSeconds();
    }
}
