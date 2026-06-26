package com.mtfm.deadman.plugin.wechat.miniprogram.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 发起人脸核身请求。
 *
 * @param certName 证件姓名
 * @param certNo   证件号码
 * @param code     可选，wx.login 临时凭证；未绑定微信账号时用于换取 openid
 */
public record WechatFaceVerifyInitiateRequest(
        @NotBlank String certName, @NotBlank String certNo, String code) {
}
