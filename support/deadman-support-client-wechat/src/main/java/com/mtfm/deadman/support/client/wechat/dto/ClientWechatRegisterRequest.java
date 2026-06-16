package com.mtfm.deadman.support.client.wechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户端微信 OAuth 绑定注册请求（新用户注册并绑定 openid）。
 *
 * @param bindToken 微信登录返回的临时绑定令牌
 * @param username  登录用户名
 * @param password  密码
 * @param nickname  昵称，可为空
 */
public record ClientWechatRegisterRequest(
        @NotBlank(message = "绑定令牌不能为空") String bindToken,
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 8, max = 64) String password,
        @Size(max = 64) String nickname) {
}
