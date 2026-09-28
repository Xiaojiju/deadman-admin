package com.mtfm.deadman.plugin.smslogin.config;

/**
 * 短信验证码登录绑定。
 *
 * @param groupId 登录 Provider 组标识，如 client、admin
 * @param loginPathSegment 登录路径段，为空时使用 sms
 */
public record SmsLoginBinding(String groupId, String loginPathSegment) {
}
