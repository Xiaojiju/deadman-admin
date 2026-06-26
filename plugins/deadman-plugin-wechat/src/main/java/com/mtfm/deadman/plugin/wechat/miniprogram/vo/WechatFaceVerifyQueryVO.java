package com.mtfm.deadman.plugin.wechat.miniprogram.vo;

/**
 * 人脸核身查询结果。
 *
 * @param verifyRet        微信侧核身结果码
 * @param verified         是否核身通过（verifyRet 为 10000）
 * @param verifyRetMessage 核身结果描述
 */
public record WechatFaceVerifyQueryVO(int verifyRet, boolean verified, String verifyRetMessage) {
}
