package com.mtfm.deadman.component.openauth.token;

import com.mtfm.deadman.component.openauth.config.OpenAuthComponentProperties;
import com.mtfm.deadman.component.openauth.constant.OpenAuthConstants;
import com.mtfm.deadman.security.jwt.JwtClaimConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * 基于 JWT 的 open_access_token 签发实现。
 */
@Component
@RequiredArgsConstructor
public class JwtOpenTokenIssuer implements OpenTokenIssuer {

    private static final int MIN_SECRET_BYTES = 32;

    private final OpenAuthComponentProperties properties;

    /**
     * 签发 open_access_token。
     *
     * @param context 签发上下文
     * @return 签发结果
     */
    @Override
    public OpenTokenIssueResult issue(OpenTokenIssueContext context) {
        SecretKey secretKey = resolveSecretKey();
        String jti = UUID.randomUUID().toString();
        long expirationMs = context.ttlSeconds() * 1000;
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        String scope = String.join(" ", context.permissions());
        String token = Jwts.builder()
                .id(jti)
                .issuer(properties.getJwt().getIssuer())
                .subject(context.subjectCode())
                .claim(JwtClaimConstants.REALM, OpenAuthConstants.JWT_REALM)
                .claim("appId", context.appId())
                .claim("authRealm", context.realm())
                .claim("subjectType", context.subjectType())
                .claim("subjectId", context.subjectId())
                .claim("subjectCode", context.subjectCode())
                .claim("scope", scope)
                .claim("ext", context.extensions())
                .claim(JwtClaimConstants.TOKEN_TYPE, JwtClaimConstants.TOKEN_TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
        return new OpenTokenIssueResult(token, context.ttlSeconds(), scope);
    }

    private SecretKey resolveSecretKey() {
        String secret = properties.getJwt().getSecret();
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("开放授权 JWT 密钥未配置或长度不足 " + MIN_SECRET_BYTES + " 字节");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
