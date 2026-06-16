package com.mtfm.deadman.security.vo.auth;

/**
 * 用户注册结果（不含令牌，需登录后签发）。
 *
 * @param userCode 用户编码
 * @param nickname 用户昵称
 */
public record RegisterResultVO(String userCode, String nickname) {
}
