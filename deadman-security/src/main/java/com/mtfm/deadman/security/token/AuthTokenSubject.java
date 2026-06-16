package com.mtfm.deadman.security.token;

/**
 * 令牌签发主体（与具体用户表解耦，按端传入）。
 *
 * @param userId   用户主键
 * @param userCode 对外用户编码
 * @param nickname 用户昵称
 */
public record AuthTokenSubject(Long userId, String userCode, String nickname) {
}
