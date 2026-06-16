package com.mtfm.deadman.security.service;

import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.token.AuthTokenIssueProvider;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import com.mtfm.deadman.security.token.AuthTokenSubject;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import com.mtfm.deadman.system.entity.UserBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证令牌门面：按端（realm）委托 {@link AuthTokenIssueProvider} 签发与刷新。
 */
@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final AuthTokenIssueProviderRegistry providerRegistry;

    /**
     * 按端签发 Access Token + Refresh Token。
     *
     * @param realm   端标识
     * @param subject 令牌主体
     * @return 双令牌视图
     */
    public AuthTokenVO issue(String realm, AuthTokenSubject subject) {
        return providerRegistry.require(realm).issue(subject);
    }

    /**
     * 管理端便捷签发（登录成功时使用）。
     *
     * @param userBase 用户基础信息
     * @return 双令牌视图
     */
    public AuthTokenVO issue(UserBase userBase) {
        return issue(
                AdminAuthConstants.JWT_REALM,
                new AuthTokenSubject(userBase.getId(), userBase.getUserCode(), userBase.getNickname()));
    }

    /**
     * 按端刷新双令牌。
     *
     * @param realm        端标识
     * @param refreshToken Refresh Token
     * @return 新的双令牌视图
     */
    public AuthTokenVO refresh(String realm, String refreshToken) {
        return providerRegistry.require(realm).refresh(refreshToken);
    }

    /**
     * 获取端 Provider（Filter / Handler 读取 Cookie 策略时使用）。
     *
     * @param realm 端标识
     * @return Provider
     */
    public AuthTokenIssueProvider requireProvider(String realm) {
        return providerRegistry.require(realm);
    }
}
