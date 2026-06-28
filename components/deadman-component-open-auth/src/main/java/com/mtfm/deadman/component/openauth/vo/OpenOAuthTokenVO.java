package com.mtfm.deadman.component.openauth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OAuth Token 兑换响应。
 *
 * @param accessToken 访问令牌
 * @param tokenType   令牌类型
 * @param expiresIn   有效期（秒）
 * @param scope       scope 字符串
 * @param subject     授权主体信息
 */
public record OpenOAuthTokenVO(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,
        String scope,
        OpenAuthSubjectVO subject) {
}
