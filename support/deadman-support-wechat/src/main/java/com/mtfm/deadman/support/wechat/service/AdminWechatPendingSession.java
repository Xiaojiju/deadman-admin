package com.mtfm.deadman.support.wechat.service;

/**
 * 管理端微信 OAuth 待绑定会话，存储 code2session 结果供后续绑定使用。
 *
 * @param openid     微信 openid
 * @param sessionKey 微信 session_key
 * @param unionid    微信 unionid，可能为空
 */
public record AdminWechatPendingSession(String openid, String sessionKey, String unionid) {
}
