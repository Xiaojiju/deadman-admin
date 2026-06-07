package com.mtfm.deadman.component.client.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户端用户名密码登录请求。
 *
 * @param username 登录用户名
 * @param password 密码
 */
public record ClientLoginPasswordRequest(@NotBlank String username, @NotBlank String password) {
}
