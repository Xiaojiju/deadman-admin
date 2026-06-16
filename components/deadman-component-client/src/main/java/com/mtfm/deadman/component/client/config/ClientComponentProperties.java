package com.mtfm.deadman.component.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 用户端组件配置，与管理系统完全隔离。
 */
@Data
@ConfigurationProperties(prefix = "deadman.component.client")
public class ClientComponentProperties {

    /** 是否启用用户端组件 */
    private boolean enabled = true;

    private Jwt jwt = new Jwt();
    private Security security = new Security();
    private User user = new User();
    private Auth auth = new Auth();
    private Cache cache = new Cache();

    @Data
    public static class Jwt {
        /** HMAC 密钥，至少 32 字符，通过 DEADMAN_CLIENT_JWT_SECRET 配置 */
        private String secret;
        /** Access Token 有效期（毫秒），默认 30 分钟 */
        private long accessExpirationMs = 1_800_000L;
        /** Refresh Token 有效期（毫秒），默认 7 天 */
        private long refreshExpirationMs = 604_800_000L;
        /** 兼容旧配置 */
        private long expirationMs = 1_800_000L;
        /** Refresh Token HttpOnly Cookie 是否启用 Secure */
        private boolean refreshCookieSecure = false;
    }

    @Data
    public static class Security {
        /** 是否允许多点登录；为 false 时新登录会使旧 JWT 失效 */
        private boolean multiSessionEnabled = true;
    }

    @Data
    public static class User {
        /** 用户端 userCode 前缀，默认 CL */
        private String userCodePrefix = "CL";
    }

    @Data
    public static class Auth {
        /** 认证 API 根路径 */
        private String basePath = "/client/api/auth";
        /** 登录端点统一前缀，完整路径为 basePath + loginPathPrefix + /{provider} */
        private String loginPathPrefix = "/login";
    }

    @Data
    public static class Cache {
        /** 用户资料缓存 TTL */
        private Duration userProfileTtl = Duration.ofMinutes(30);
    }
}
