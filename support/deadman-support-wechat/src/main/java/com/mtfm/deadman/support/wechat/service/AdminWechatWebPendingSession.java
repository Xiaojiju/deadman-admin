package com.mtfm.deadman.support.wechat.service;

/**
 * 管理端微信网页扫码 OAuth 待绑定会话。
 *
 * @param openid     微信 openid
 * @param unionid    微信 unionid，可能为空
 * @param nickname   微信昵称
 * @param headimgurl 微信头像 URL
 */
public record AdminWechatWebPendingSession(String openid, String unionid, String nickname, String headimgurl) {
}
