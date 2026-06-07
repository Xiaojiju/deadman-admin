package com.mtfm.deadman.security.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求。
 *
 * @param username 登录用户名，全局唯一（USERNAME 账号类型）
 * @param password 明文密码，入库前经 PasswordEncoder 编码
 * @param nickname 显示昵称，可选；为空时使用用户名
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空") @Size(min = 3, max = 64, message = "用户名长度为 3-64") String username,
        @NotBlank(message = "密码不能为空") @Size(min = 8, max = 128, message = "密码长度为 8-128") String password,
        @Size(max = 64, message = "昵称最长 64 字符") String nickname) {
}
