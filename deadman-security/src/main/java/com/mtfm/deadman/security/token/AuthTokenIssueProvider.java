package com.mtfm.deadman.security.token;

import com.mtfm.deadman.security.jwt.RealmJwtSupport;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;

/**
 * 按端（realm）签发与刷新 JWT 的扩展点；可替换为自定义实现或在特定条件下切换策略。
 */
public interface AuthTokenIssueProvider {

    /**
     * 端标识，如 {@code ADMIN}、{@code CLIENT}。
     *
     * @return realm
     */
    String realm();

    /**
     * 签发 Access Token + Refresh Token。
     *
     * @param subject 令牌主体
     * @return 双令牌视图
     */
    AuthTokenVO issue(AuthTokenSubject subject);

    /**
     * 使用 Refresh Token 轮换签发全新双令牌。
     *
     * @param refreshToken Refresh Token
     * @return 新的双令牌视图
     */
    AuthTokenVO refresh(String refreshToken);

    /**
     * 该端 JWT 运行时支撑（解析、会话存储等）。
     *
     * @return JWT 支撑
     */
    RealmJwtSupport jwtSupport();

    /**
     * 无感刷新 HTTP 路径。
     *
     * @return 路径
     */
    String refreshTokenPath();

    /**
     * Refresh Token HttpOnly Cookie 名称。
     *
     * @return Cookie 名
     */
    String refreshTokenCookieName();

    /**
     * Refresh Token HttpOnly Cookie Path。
     *
     * @return Cookie Path
     */
    String refreshTokenCookiePath();

    /**
     * Refresh Cookie 是否启用 Secure。
     *
     * @return 是否 Secure
     */
    boolean isRefreshCookieSecure();

    /**
     * 使用户该端全部会话与 Refresh Token 失效（禁用/注销时调用）。
     *
     * @param userId 用户主键
     */
    void invalidateUserSessions(Long userId);
}
