package com.mtfm.deadman.support.wechat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理端微信 OAuth 绑定请求（用户名密码二次认证）。
 *
 * @param bindToken 微信登录返回的临时绑定令牌
 * @param username  管理端用户名
 * @param password  管理端密码
 */
public record AdminWechatBindRequest(
        @NotBlank(message = "绑定令牌不能为空") String bindToken,
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password) {
}
