package com.mtfm.deadman.security.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求。
 *
 * @param oldPassword 当前密码
 * @param newPassword 新密码，将重新随机选取 PasswordEncoder 编码
 */
public record ChangePasswordRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") @Size(min = 8, max = 128, message = "新密码长度为 8-128") String newPassword) {
}
