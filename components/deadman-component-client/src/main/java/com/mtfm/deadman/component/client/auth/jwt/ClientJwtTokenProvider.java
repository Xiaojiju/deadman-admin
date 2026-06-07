package com.mtfm.deadman.component.client.auth.jwt;

import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
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
 * 用户端 JWT 签发与解析，与管理端密钥及过期时间完全独立。
 */
@Component
public class ClientJwtTokenProvider {

    private static final int MIN_SECRET_BYTES = 32;

    private final ClientComponentProperties properties;
    private SecretKey secretKey;
    private long expirationMs;

    public ClientJwtTokenProvider(ClientComponentProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化 JWT 密钥。
     */
    @PostConstruct
    void init() {
        String secret = properties.getJwt().getSecret();
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "请配置 DEADMAN_CLIENT_JWT_SECRET 环境变量，且长度至少 " + MIN_SECRET_BYTES + " 字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.getJwt().getExpirationMs();
    }

    /**
     * 签发用户端访问令牌。
     *
     * @param userId   用户主键
     * @param userCode 用户编码
     * @param jti      会话唯一标识
     * @return JWT 字符串
     */
    public String createToken(Long userId, String userCode, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim(ClientAuthConstants.JWT_USER_CODE, userCode)
                .claim("realm", ClientAuthConstants.JWT_REALM)
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

    /**
     * 从 Claims 获取用户 ID。
     *
     * @param claims JWT Claims
     * @return 用户 ID
     */
    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Claims 获取用户编码。
     *
     * @param claims JWT Claims
     * @return 用户编码
     */
    public String getUserCode(Claims claims) {
        return claims.get(ClientAuthConstants.JWT_USER_CODE, String.class);
    }

    /**
     * 从 Claims 获取 jti。
     *
     * @param claims JWT Claims
     * @return jti
     */
    public String getJti(Claims claims) {
        return claims.getId();
    }

    /**
     * 校验 realm 是否为用户端。
     *
     * @param claims JWT Claims
     * @return 是否为用户端令牌
     */
    public boolean isClientRealm(Claims claims) {
        return ClientAuthConstants.JWT_REALM.equals(claims.get("realm", String.class));
    }
}
