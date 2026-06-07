package com.mtfm.deadman.plugin.wechat.miniprogram.client;

/**
 * 微信 jscode2session 响应。
 *
 * @param openid     用户唯一标识
 * @param sessionKey 会话密钥
 * @param unionid    开放平台唯一标识，可能为空
 */
public record WechatCode2SessionResult(String openid, String sessionKey, String unionid) {
}
