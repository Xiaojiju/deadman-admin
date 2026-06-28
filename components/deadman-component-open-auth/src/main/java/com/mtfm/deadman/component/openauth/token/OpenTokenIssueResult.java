package com.mtfm.deadman.component.openauth.token;

/**
 * 开放 access_token 签发结果。
 *
 * @param accessToken 访问令牌
 * @param expiresIn   有效期（秒）
 * @param scope       scope 字符串（空格分隔）
 */
public record OpenTokenIssueResult(String accessToken, long expiresIn, String scope) {
}
