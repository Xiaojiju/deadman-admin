package com.mtfm.deadman.support.client.wechat.auth;

/**
 * 用户端微信 OAuth 待绑定登录主体，携带临时绑定令牌信息。
 *
 * @param bindToken 临时绑定令牌
 * @param expiresIn 有效秒数
 */
public record ClientWechatPendingBindPrincipal(String bindToken, long expiresIn) {
}
