package com.mtfm.deadman.component.client.service;

import com.mtfm.deadman.component.client.auth.jwt.ClientJwtSessionStore;
import com.mtfm.deadman.component.client.auth.jwt.ClientJwtTokenProvider;
import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.vo.ClientAuthTokenVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户端 JWT 签发与会话注册。
 */
@Service
@RequiredArgsConstructor
public class ClientAuthTokenService {

    private final ClientJwtTokenProvider clientJwtTokenProvider;
    private final ClientJwtSessionStore clientJwtSessionStore;
    private final ClientComponentProperties clientComponentProperties;

    /**
     * 为用户签发访问令牌。
     *
     * @param userBase 用户基础信息
     * @return 令牌视图
     */
    public ClientAuthTokenVO issueToken(ClientUserBase userBase) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        String token = clientJwtTokenProvider.createToken(userBase.getId(), userBase.getUserCode(), jti);
        clientJwtSessionStore.registerSession(userBase.getId(), jti);
        return ClientAuthTokenVO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(clientComponentProperties.getJwt().getExpirationMs() / 1000)
                .userCode(userBase.getUserCode())
                .nickname(userBase.getNickname())
                .build();
    }
}
