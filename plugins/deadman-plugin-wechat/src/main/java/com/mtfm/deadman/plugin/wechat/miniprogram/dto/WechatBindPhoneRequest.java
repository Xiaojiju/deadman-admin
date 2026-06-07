package com.mtfm.deadman.plugin.wechat.miniprogram.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信手机号绑定请求（getPhoneNumber 按钮返回的 code）。
 *
 * @param code 手机号动态令牌
 */
public record WechatBindPhoneRequest(@NotBlank String code) {
}
