package com.mtfm.deadman.support.client.wechat.auth;

import com.mtfm.deadman.support.client.wechat.dto.ClientWechatBindRequest;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * 用户端微信 OAuth 绑定认证请求令牌（未认证态）。
 */
@Getter
public class ClientWechatBindAuthenticationToken extends AbstractAuthenticationToken {

    private final ClientWechatBindRequest bindRequest;

    /**
     * 构造未认证的绑定请求令牌。
     *
     * @param bindRequest 绑定请求体
     */
    public ClientWechatBindAuthenticationToken(ClientWechatBindRequest bindRequest) {
        super(Collections.emptyList());
        this.bindRequest = bindRequest;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return bindRequest.password();
    }

    @Override
    public Object getPrincipal() {
        return bindRequest.username();
    }
}
