package com.mtfm.deadman.support.wechat.vo;

/**
 * 管理端微信 OAuth 待绑定响应。
 *
 * @param bindToken 临时绑定令牌，用于后续用户名密码认证
 * @param expiresIn 令牌有效秒数
 * @param needBind  是否需要进一步绑定本地账号，固定为 true
 */
public record AdminWechatPendingBindVO(String bindToken, long expiresIn, boolean needBind) {
}
