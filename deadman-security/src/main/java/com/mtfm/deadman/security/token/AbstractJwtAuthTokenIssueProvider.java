package com.mtfm.deadman.security.token;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.security.jwt.RefreshTokenCheckResult;
import com.mtfm.deadman.security.jwt.RealmJwtSupport;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 基于 JWT 的通用 {@link AuthTokenIssueProvider} 实现，子类仅需提供刷新时的用户加载逻辑。
 */
public abstract class AbstractJwtAuthTokenIssueProvider implements AuthTokenIssueProvider {

    private static final Logger log = LoggerFactory.getLogger(AbstractJwtAuthTokenIssueProvider.class);

    private final RealmJwtSupport jwtSupport;

    /**
     * @param jwtSupport JWT 运行时支撑
     */
    protected AbstractJwtAuthTokenIssueProvider(RealmJwtSupport jwtSupport) {
        this.jwtSupport = jwtSupport;
    }

    @Override
    public String realm() {
        return jwtSupport.settings().realm();
    }

    @Override
    public RealmJwtSupport jwtSupport() {
        return jwtSupport;
    }

    @Override
    public String refreshTokenPath() {
        return jwtSupport.settings().refreshTokenPath();
    }

    @Override
    public String refreshTokenCookieName() {
        return jwtSupport.settings().refreshTokenCookieName();
    }

    @Override
    public String refreshTokenCookiePath() {
        return jwtSupport.settings().refreshTokenCookiePath();
    }

    @Override
    public boolean isRefreshCookieSecure() {
        return jwtSupport.settings().refreshCookieSecure();
    }

    @Override
    public AuthTokenVO issue(AuthTokenSubject subject) {
        String accessJti = newJti();
        String refreshJti = newJti();
        String accessToken = jwtSupport
                .tokenProvider()
                .createAccessToken(subject.userId(), subject.userCode(), accessJti);
        String refreshToken = jwtSupport
                .tokenProvider()
                .createRefreshToken(subject.userId(), subject.userCode(), refreshJti);
        jwtSupport.sessionStore().registerSession(subject.userId(), accessJti);
        jwtSupport.refreshTokenStore().registerRefreshToken(subject.userId(), refreshJti);
        return buildTokenVO(subject, accessToken, refreshToken);
    }

    @Override
    public AuthTokenVO refresh(String refreshToken) {
        Claims claims = parseRefreshClaims(refreshToken);
        Long userId = jwtSupport.tokenProvider().getUserId(claims);
        String refreshJti = jwtSupport.tokenProvider().getJti(claims);
        RefreshTokenCheckResult checkResult = jwtSupport.refreshTokenStore().checkRefreshToken(userId, refreshJti);
        if (checkResult == RefreshTokenCheckResult.REUSED) {
            log.warn("检测到 Refresh Token 重用，realm={} userId={} jti={}", realm(), userId, refreshJti);
            invalidateUserSessions(userId);
            throw new BusinessException(ResultCode.TOKEN_REUSE_DETECTED);
        }
        if (checkResult != RefreshTokenCheckResult.ACTIVE) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        AuthTokenSubject subject = loadSubjectForRefresh(userId);
        String newAccessJti = newJti();
        String newRefreshJti = newJti();
        if (!jwtSupport.refreshTokenStore().rotateRefreshToken(userId, refreshJti, newRefreshJti)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        // Refresh 轮换时强制替换 Access 会话，避免旧 Access Token 与新一轮并存
        jwtSupport.sessionStore().replaceSession(userId, newAccessJti);
        String accessToken = jwtSupport
                .tokenProvider()
                .createAccessToken(subject.userId(), subject.userCode(), newAccessJti);
        String newRefreshToken = jwtSupport
                .tokenProvider()
                .createRefreshToken(subject.userId(), subject.userCode(), newRefreshJti);
        return buildTokenVO(subject, accessToken, newRefreshToken);
    }

    @Override
    public void invalidateUserSessions(Long userId) {
        jwtSupport.sessionStore().invalidateUserSessions(userId);
        jwtSupport.refreshTokenStore().revokeAllForUser(userId);
    }

    /**
     * 刷新令牌时加载用户主体。
     *
     * @param userId 用户主键
     * @return 令牌主体
     */
    protected abstract AuthTokenSubject loadSubjectForRefresh(Long userId);

    private Claims parseRefreshClaims(String refreshToken) {
        try {
            Claims claims = jwtSupport.tokenProvider().parseClaims(refreshToken);
            if (!jwtSupport.tokenProvider().isRealm(claims)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }
            if (!jwtSupport.tokenProvider().isRefreshToken(claims)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }
            return claims;
        } catch (JwtException ex) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    private AuthTokenVO buildTokenVO(AuthTokenSubject subject, String accessToken, String refreshToken) {
        return AuthTokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtSupport.tokenProvider().getAccessExpiresInSeconds())
                .refreshExpiresIn(jwtSupport.tokenProvider().getRefreshExpiresInSeconds())
                .userCode(subject.userCode())
                .nickname(subject.nickname())
                .build();
    }

    private String newJti() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
