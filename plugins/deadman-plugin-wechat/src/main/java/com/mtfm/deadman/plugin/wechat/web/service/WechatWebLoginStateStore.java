package com.mtfm.deadman.plugin.wechat.web.service;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.web.config.WechatWebPluginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信网页扫码登录 OAuth state 内存存储（一次性消费，防 CSRF）。
 */
@Component
@RequiredArgsConstructor
public class WechatWebLoginStateStore {

    private final WechatWebPluginProperties properties;
    private final Map<String, Instant> states = new ConcurrentHashMap<>();

    /**
     * 签发新的 OAuth state。
     *
     * @return state 字符串
     */
    public String issueState() {
        purgeExpired();
        String state = UUID.randomUUID().toString().replace("-", "");
        states.put(state, Instant.now().plus(properties.getStateTtl()));
        return state;
    }

    /**
     * 校验并消费 state（一次性）。
     *
     * @param state OAuth state
     */
    public void consumeState(String state) {
        if (!StringUtils.hasText(state)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 state 不能为空");
        }
        purgeExpired();
        Instant expiresAt = states.remove(state.trim());
        if (expiresAt == null || Instant.now().isAfter(expiresAt)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 state 无效或已过期");
        }
    }

    /**
     * 获取 state 有效秒数。
     *
     * @return TTL 秒数
     */
    public long stateExpiresInSeconds() {
        return properties.getStateTtl().toSeconds();
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        states.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}
