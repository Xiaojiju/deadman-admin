package com.mtfm.deadman.support.client.wechat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 已登录用户绑定微信小程序 openid 请求（wx.login 返回的 code）。
 *
 * @param code 微信临时登录凭证
 */
public record ClientWechatBindOpenidRequest(@NotBlank String code) {
}
