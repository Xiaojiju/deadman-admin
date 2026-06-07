package com.mtfm.deadman.security.service;

import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import com.mtfm.deadman.security.jwt.JwtSessionStore;
import com.mtfm.deadman.security.jwt.JwtTokenProvider;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import com.mtfm.deadman.system.entity.UserBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * JWT 签发与会话注册。
 */
@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtSessionStore jwtSessionStore;
    private final DeadmanProperties deadmanProperties;

    /**
     * 为用户签发访问令牌并注册会话（单点登录模式下覆盖旧会话）。
     *
     * @param userBase 用户基础信息
     * @return 令牌视图
     */
    public AuthTokenVO issueToken(UserBase userBase) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        String token = jwtTokenProvider.createToken(userBase.getId(), userBase.getUserCode(), jti);
        jwtSessionStore.registerSession(userBase.getId(), jti);
        return AuthTokenVO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(deadmanProperties.getJwt().getExpirationMs() / 1000)
                .userCode(userBase.getUserCode())
                .nickname(userBase.getNickname())
                .build();
    }
}
