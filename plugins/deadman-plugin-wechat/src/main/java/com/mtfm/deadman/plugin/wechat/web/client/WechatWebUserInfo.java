package com.mtfm.deadman.plugin.wechat.web.client;

/**
 * 微信网页授权用户信息。
 *
 * @param openid     用户唯一标识
 * @param nickname   昵称
 * @param headimgurl 头像 URL
 * @param unionid    开放平台统一标识
 */
public record WechatWebUserInfo(String openid, String nickname, String headimgurl, String unionid) {
}
