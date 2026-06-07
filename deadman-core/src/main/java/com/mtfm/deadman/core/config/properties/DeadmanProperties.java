package com.mtfm.deadman.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * deadman 应用配置项，敏感信息通过环境变量注入。
 */
@Data
@ConfigurationProperties(prefix = "deadman")
public class DeadmanProperties {

    private Jwt jwt = new Jwt();
    private Security security = new Security();
    private User user = new User();
    private Cache cache = new Cache();
    private Bootstrap bootstrap = new Bootstrap();

    @Data
    public static class Jwt {
        /** HMAC 密钥，至少 32 字符，通过 DEADMAN_JWT_SECRET 配置 */
        private String secret;
        private long expirationMs = 86_400_000L;
    }

    @Data
    public static class Security {
        /**
         * 是否允许多点登录。为 false 时，新登录会使旧 JWT 失效（单点登录）。
         */
        private boolean multiSessionEnabled = true;
    }

    @Data
    public static class User {
        private String userCodePrefix = "DM";
    }

    @Data
    public static class Cache {
        /** 用户资料缓存 TTL，禁止永久缓存 */
        private Duration userProfileTtl = Duration.ofMinutes(30);
    }

    @Data
    public static class Bootstrap {
        /**
         * 是否启用启动时超级管理员检查/创建，默认开启。
         */
        private boolean adminEnabled = true;

        /**
         * 超级管理员登录用户名（USERNAME 账号）。系统中尚无任何 SUPER_ADMIN 用户时生效。
         */
        private String superAdminUsername;

        /**
         * 自动创建超级管理员时的初始密码；留空则仅尝试为已存在用户绑定角色，不新建账号。
         */
        private String superAdminPassword;

        /**
         * 自动创建时的昵称，默认同 {@link #superAdminUsername}。
         */
        private String superAdminNickname;
    }
}
