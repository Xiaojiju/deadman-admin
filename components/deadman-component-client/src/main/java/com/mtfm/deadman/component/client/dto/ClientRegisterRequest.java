package com.mtfm.deadman.component.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户端注册请求。
 *
 * @param username 登录用户名
 * @param password 密码
 * @param nickname 昵称，可为空
 */
public record ClientRegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 8, max = 64) String password,
        @Size(max = 64) String nickname) {
}
