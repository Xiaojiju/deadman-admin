package com.mtfm.deadman.plugin.wechat.miniprogram.dto;

import com.mtfm.deadman.plugin.wechat.miniprogram.WechatFaceVerifyConstants;

/**
 * 人脸核身用户身份信息。
 *
 * @param certType 证件类型
 * @param certName 证件姓名
 * @param certNo   证件号码
 */
public record WechatFaceCertInfo(String certType, String certName, String certNo) {

    /**
     * 构造身份证类型的证件信息。
     *
     * @param certName 证件姓名
     * @param certNo   证件号码
     * @return 身份证证件信息
     */
    public static WechatFaceCertInfo identityCard(String certName, String certNo) {
        return new WechatFaceCertInfo(WechatFaceVerifyConstants.CERT_TYPE_IDENTITY_CARD, certName, certNo);
    }
}
