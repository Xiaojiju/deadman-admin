package com.mtfm.deadman.plugin.wechat.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信网页扫码登录请求（授权回调获得的 code 与 state）。
 *
 * @param code  微信授权临时凭证
 * @param state OAuth 防 CSRF 状态值
 */
public record WechatWebLoginRequest(@NotBlank String code, @NotBlank String state) {
}
