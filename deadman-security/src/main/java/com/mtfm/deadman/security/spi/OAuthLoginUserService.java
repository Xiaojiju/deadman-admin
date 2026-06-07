package com.mtfm.deadman.security.spi;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * OAuth 登录用户解析 SPI，各用户体系实现查找或注入 OAuth 用户并返回已认证令牌。
 */
public interface OAuthLoginUserService {

    /**
     * 所属登录 Provider 组标识。
     *
     * @return 组标识
     */
    String loginGroupId();

    /**
     * 查找或创建 OAuth 用户并返回已认证 Authentication。
     *
     * @param request OAuth 登录请求
     * @return 已认证令牌
     */
    Authentication resolveOAuthLogin(OAuthLoginRequest request) throws AuthenticationException;

    /**
     * OAuth 登录用户解析请求。
     *
     * @param oauthProvider     OAuth 提供商
     * @param oauthSubject      OAuth 用户唯一标识
     * @param loginIdentifier   登录标识（如 openid）
     * @param nickname          昵称
     * @param avatar            头像 URL
     */
    record OAuthLoginRequest(
            String oauthProvider, String oauthSubject, String loginIdentifier, String nickname, String avatar) {
    }
}
