package com.mtfm.deadman.plugin.wechat.miniprogram.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 查询人脸核身结果请求。
 *
 * @param verifyId  人脸核身会话唯一标识
 * @param outSeqNo  业务流水号，须与发起核身时一致
 * @param certName  证件姓名，须与发起核身时一致
 * @param certNo    证件号码，须与发起核身时一致
 * @param code      可选，wx.login 临时凭证；未绑定微信账号时用于换取 openid
 */
public record WechatFaceVerifyQueryRequest(
        @NotBlank String verifyId,
        @NotBlank String outSeqNo,
        @NotBlank String certName,
        @NotBlank String certNo,
        String code) {
}
