package com.mtfm.deadman.plugin.wechat.miniprogram.vo;

/**
 * 发起人脸核身结果，供小程序调用 wx.requestFacialVerify。
 *
 * @param verifyId  人脸核身会话唯一标识
 * @param expiresIn verifyId 有效期（秒）
 * @param outSeqNo  业务流水号，查询结果时须回传
 */
public record WechatFaceVerifyInitiateVO(String verifyId, int expiresIn, String outSeqNo) {
}
