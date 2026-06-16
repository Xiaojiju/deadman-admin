package com.mtfm.deadman.security.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证令牌VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenVO {

    /** 访问令牌 */
    private String accessToken;
    /** 刷新令牌（同时写入 HttpOnly Cookie，供无感刷新） */
    private String refreshToken;
    /** 令牌类型 */
    private String tokenType;
    /** Access Token 过期时间（秒） */
    private Long expiresIn;
    /** Refresh Token 过期时间（秒） */
    private Long refreshExpiresIn;
    /** 用户编码 */
    private String userCode;
    /** 用户昵称 */
    private String nickname;
}
