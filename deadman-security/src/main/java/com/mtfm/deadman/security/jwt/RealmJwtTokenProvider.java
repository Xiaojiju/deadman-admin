package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.security.token.RealmJwtSettings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 按端（realm）隔离的 JWT 签发与解析。
 */
public class RealmJwtTokenProvider {

    private static final int MIN_SECRET_BYTES = 32;

    private final RealmJwtSettings settings;
    private final SecretKey secretKey;

    /**
     * 根据端配置初始化 JWT 引擎。
     *
     * @param settings 端 JWT 配置
     */
    public RealmJwtTokenProvider(RealmJwtSettings settings) {
        this.settings = settings;
        String secret = settings.secret();
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT 密钥配置不正确，realm=" + settings.realm() + "，长度至少 " + MIN_SECRET_BYTES + " 字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 签发 Access Token。
     *
     * @param userId   用户主键
     * @param userCode 用户编码
     * @param jti      唯一标识
     * @return JWT
     */
    public String createAccessToken(Long userId, String userCode, String jti) {
        return createToken(userId, userCode, jti, settings.accessExpirationMs(), JwtClaimConstants.TOKEN_TYPE_ACCESS);
    }

    /**
     * 签发 Refresh Token。
     *
     * @param userId   用户主键
     * @param userCode 用户编码
     * @param jti      唯一标识
     * @return JWT
     */
    public String createRefreshToken(Long userId, String userCode, String jti) {
        return createToken(userId, userCode, jti, settings.refreshExpirationMs(), JwtClaimConstants.TOKEN_TYPE_REFRESH);
    }

    /**
     * 解析并校验 JWT。
     *
     * @param token JWT 字符串
     * @return Claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Access Token 有效期（秒）。
     *
     * @return 秒数
     */
    public long getAccessExpiresInSeconds() {
        return settings.accessExpirationMs() / 1000;
    }

    /**
     * Refresh Token 有效期（秒）。
     *
     * @return 秒数
     */
    public long getRefreshExpiresInSeconds() {
        return settings.refreshExpirationMs() / 1000;
    }

    /**
     * 是否为该端 realm。
     *
     * @param claims JWT 载荷
     * @return 是否匹配
     */
    public boolean isRealm(Claims claims) {
        return settings.realm().equals(claims.get("realm", String.class));
    }

    /**
     * 是否为 Access Token。
     *
     * @param claims JWT 载荷
     * @return 是否 Access
     */
    public boolean isAccessToken(Claims claims) {
        return JwtClaimConstants.TOKEN_TYPE_ACCESS.equals(getTokenType(claims));
    }

    /**
     * 是否为 Refresh Token。
     *
     * @param claims JWT 载荷
     * @return 是否 Refresh
     */
    public boolean isRefreshToken(Claims claims) {
        return JwtClaimConstants.TOKEN_TYPE_REFRESH.equals(getTokenType(claims));
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public String getUserCode(Claims claims) {
        return claims.get("userCode", String.class);
    }

    public String getJti(Claims claims) {
        return claims.getId();
    }

    public String getTokenType(Claims claims) {
        return claims.get(JwtClaimConstants.TOKEN_TYPE, String.class);
    }

    public String realm() {
        return settings.realm();
    }

    private String createToken(Long userId, String userCode, String jti, long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim(JwtClaimConstants.USER_CODE, userCode)
                .claim(JwtClaimConstants.REALM, settings.realm())
                .claim(JwtClaimConstants.TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
}
