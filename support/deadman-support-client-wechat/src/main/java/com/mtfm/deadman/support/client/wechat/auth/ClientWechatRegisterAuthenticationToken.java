package com.mtfm.deadman.support.client.wechat.auth;

import com.mtfm.deadman.support.client.wechat.dto.ClientWechatRegisterRequest;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * 用户端微信 OAuth 绑定注册认证请求令牌（未认证态）。
 */
@Getter
public class ClientWechatRegisterAuthenticationToken extends AbstractAuthenticationToken {

    private final ClientWechatRegisterRequest registerRequest;

    /**
     * 构造未认证的绑定注册请求令牌。
     *
     * @param registerRequest 绑定注册请求体
     */
    public ClientWechatRegisterAuthenticationToken(ClientWechatRegisterRequest registerRequest) {
        super(Collections.emptyList());
        this.registerRequest = registerRequest;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return registerRequest.password();
    }

    @Override
    public Object getPrincipal() {
        return registerRequest.username();
    }
}
