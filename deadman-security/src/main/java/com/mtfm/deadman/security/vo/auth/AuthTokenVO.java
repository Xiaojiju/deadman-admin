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
    /** 令牌类型 */
    private String tokenType;
    /** 过期时间 */
    private Long expiresIn;
    /** 用户编码 */
    private String userCode;
    /** 用户昵称 */
    private String nickname;
}
