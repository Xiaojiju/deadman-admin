package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析。
 */
@Component
public class JwtTokenProvider {

    private static final int MIN_SECRET_BYTES = 32;

    private final DeadmanProperties properties;
    private SecretKey secretKey;
    private long expirationMs;

    public JwtTokenProvider(DeadmanProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化 JWT 密钥和过期时间
     * 
     * @throws IllegalStateException 如果密钥配置不正确
     */
    @PostConstruct
    void init() {
        String secret = properties.getJwt().getSecret();
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "请配置 DEADMAN_JWT_SECRET 环境变量，且长度至少 " + MIN_SECRET_BYTES + " 字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.getJwt().getExpirationMs();
    }

    /**
     * 签发访问令牌。
     *
     * @param userId   用户主键
     * @param userCode 对外用户编码
     * @return JWT 字符串
     */
    public String createToken(Long userId, String userCode, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim("userCode", userCode)
                .claim("realm", AdminAuthConstants.JWT_REALM)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析并校验 JWT。
     *
     * @param token Bearer 令牌
     * @return Claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
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
}
