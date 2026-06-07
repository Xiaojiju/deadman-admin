package com.mtfm.deadman.system.vo.user;

/**
 * 用户已绑定的登录账号摘要。
 *
 * @param accountType       账号类型：USERNAME / PHONE / OAUTH
 * @param accountIdentifier 账号标识（用户名、手机号等；OAuth 可为 subject 摘要）
 * @param oauthProvider     第三方提供商标识（仅 OAUTH 时有值，如 wechat、github）
 * @param verified          是否已验证：0-否 1-是
 * @param status            账号状态：0-禁用 1-正常
 */
public record UserAccountBindingVO(
        String accountType,
        String accountIdentifier,
        String oauthProvider,
        Integer verified,
        Integer status) {
}
