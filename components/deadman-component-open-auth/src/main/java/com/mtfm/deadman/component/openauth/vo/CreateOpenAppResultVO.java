package com.mtfm.deadman.component.openauth.vo;

import java.util.List;

/**
 * 创建开放应用结果，client_secret 仅此次返回。
 *
 * @param appId        AppId
 * @param appName      应用名称
 * @param clientSecret 应用密钥明文（仅展示一次）
 * @param allowedRealms 允许的用户域
 * @param defaultScopes 默认 scope
 */
public record CreateOpenAppResultVO(
        String appId, String appName, String clientSecret, List<String> allowedRealms, List<String> defaultScopes) {
}
