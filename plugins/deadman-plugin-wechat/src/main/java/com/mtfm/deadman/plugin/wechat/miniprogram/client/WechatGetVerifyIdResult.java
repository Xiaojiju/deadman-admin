package com.mtfm.deadman.plugin.wechat.miniprogram.client;

/**
 * 微信 getVerifyId 接口响应。
 *
 * @param verifyId  人脸核身会话唯一标识
 * @param expiresIn verifyId 有效期（秒）
 */
public record WechatGetVerifyIdResult(String verifyId, int expiresIn) {
}
