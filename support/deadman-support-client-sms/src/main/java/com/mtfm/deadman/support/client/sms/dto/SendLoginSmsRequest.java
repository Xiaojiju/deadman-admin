package com.mtfm.deadman.support.client.sms.dto;

import com.mtfm.deadman.common.validation.PhoneNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送登录短信验证码。
 *
 * @param phone 手机号
 */
public record SendLoginSmsRequest(
    @NotBlank @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone) {
}
