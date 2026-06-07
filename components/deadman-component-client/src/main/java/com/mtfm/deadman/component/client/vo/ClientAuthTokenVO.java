package com.mtfm.deadman.component.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户端认证令牌响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAuthTokenVO {

    /** 访问令牌 */
    private String accessToken;
    /** 令牌类型 */
    private String tokenType;
    /** 过期秒数 */
    private Long expiresIn;
    /** 用户编码 */
    private String userCode;
    /** 昵称 */
    private String nickname;
}
