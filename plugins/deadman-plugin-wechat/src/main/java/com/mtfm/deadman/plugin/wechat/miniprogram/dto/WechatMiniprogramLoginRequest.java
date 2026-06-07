package com.mtfm.deadman.plugin.wechat.miniprogram.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信小程序登录请求（wx.login 返回的 code）。
 *
 * @param code 临时登录凭证
 */
public record WechatMiniprogramLoginRequest(@NotBlank String code) {
}
