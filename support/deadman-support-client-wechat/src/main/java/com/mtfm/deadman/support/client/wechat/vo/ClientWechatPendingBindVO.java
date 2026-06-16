package com.mtfm.deadman.support.client.wechat.vo;

/**
 * 用户端微信 OAuth 待绑定响应。
 *
 * @param bindToken 临时绑定令牌，用于后续用户名密码认证
 * @param expiresIn 令牌有效秒数
 * @param needBind  是否需要进一步绑定本地账号，固定为 true
 */
public record ClientWechatPendingBindVO(String bindToken, long expiresIn, boolean needBind) {
}
