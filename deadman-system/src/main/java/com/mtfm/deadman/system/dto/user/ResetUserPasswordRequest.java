package com.mtfm.deadman.system.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理端重置用户密码请求（无需原密码）。
 *
 * @param newPassword 新密码
 */
public record ResetUserPasswordRequest(
        @NotBlank(message = "新密码不能为空") @Size(min = 8, max = 128, message = "新密码长度为 8-128") String newPassword) {
}
