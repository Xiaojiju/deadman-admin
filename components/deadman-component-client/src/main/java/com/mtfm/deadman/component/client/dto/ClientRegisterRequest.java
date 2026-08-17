package com.mtfm.deadman.component.client.dto;

import com.mtfm.deadman.common.validation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户端注册请求。
 *
 * @param username     用户名
 * @param password     密码
 * @param nickname     昵称，可为空
 * @param avatarFileId 头像文件 ID，可为空
 * @param phone        手机号（绑定为 PHONE 账号类型）
 * @param inviteCode   推广码，可为空
 */
public record ClientRegisterRequest(
    @NotBlank @Size(min = 3, max = 64) String username,
    @NotBlank @Size(min = 8, max = 64) String password,
    @Size(max = 64) String nickname,
    Long avatarFileId,
    @NotBlank(message = "手机号不能为空") @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone,
    @Size(max = 32) String inviteCode) {
}
