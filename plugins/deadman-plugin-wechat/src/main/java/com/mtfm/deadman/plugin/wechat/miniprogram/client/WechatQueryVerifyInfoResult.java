package com.mtfm.deadman.plugin.wechat.miniprogram.client;

/**
 * 微信 queryVerifyInfo 接口响应。
 *
 * @param verifyRet 人脸核身验证结果码
 */
public record WechatQueryVerifyInfoResult(int verifyRet) {
}
