package com.mtfm.deadman.plugin.wechat.login.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信登录凭证解析请求（HTTP 与内部调用均可使用）。
 *
 * @param loginKind 登录方式标识，如 wechat-miniprogram、wechat-web
 * @param code      微信临时凭证（各方式通用）
 * @param state     网页扫码登录 OAuth state，仅 wechat-web 必填
 */
public record WechatLoginResolveRequest(@NotBlank String loginKind, @NotBlank String code, String state) {
}
