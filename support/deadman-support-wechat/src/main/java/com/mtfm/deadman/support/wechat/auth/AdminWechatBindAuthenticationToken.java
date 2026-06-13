package com.mtfm.deadman.support.wechat.auth;

import com.mtfm.deadman.support.wechat.dto.AdminWechatBindRequest;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * 管理端微信 OAuth 绑定认证请求令牌（未认证态）。
 */
@Getter
public class AdminWechatBindAuthenticationToken extends AbstractAuthenticationToken {

    private final AdminWechatBindRequest bindRequest;

    /**
     * 构造未认证的绑定请求令牌。
     *
     * @param bindRequest 绑定请求体
     */
    public AdminWechatBindAuthenticationToken(AdminWechatBindRequest bindRequest) {
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
