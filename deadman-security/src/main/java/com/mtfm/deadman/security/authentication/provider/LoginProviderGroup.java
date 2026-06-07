package com.mtfm.deadman.security.authentication.provider;

/**
 * 登录 Provider 组描述，绑定 API 前缀与认证路径。
 *
 * @param groupId         组标识，如 admin、client
 * @param apiPrefix       API 路径前缀，用于 SecurityFilterChain 匹配，如 /api、/client/api
 * @param authBasePath    认证 API 根路径，如 /api/auth、/client/api/auth
 * @param loginPathPrefix 登录端点统一前缀，完整路径为 authBasePath + loginPathPrefix + /{segment}
 */
public record LoginProviderGroup(String groupId, String apiPrefix, String authBasePath, String loginPathPrefix) {
}
