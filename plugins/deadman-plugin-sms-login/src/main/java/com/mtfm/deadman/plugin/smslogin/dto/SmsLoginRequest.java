package com.mtfm.deadman.plugin.smslogin.dto;

/**
 * 短信验证码登录请求体。
 *
 * @param phone 手机号
 * @param code 验证码
 */
public record SmsLoginRequest(String phone, String code) {
}
