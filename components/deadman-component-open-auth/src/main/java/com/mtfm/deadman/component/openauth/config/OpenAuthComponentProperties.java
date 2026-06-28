package com.mtfm.deadman.component.openauth.config;

import com.mtfm.deadman.component.openauth.constant.OpenAuthConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 开放授权组件配置属性。
 */
@Data
@ConfigurationProperties(prefix = "deadman.component.open-auth")
public class OpenAuthComponentProperties {

    /** 是否启用组件 */
    private boolean enabled = true;

    /** JWT 相关配置 */
    private Jwt jwt = new Jwt();

    /** 授权码相关配置 */
    private Code code = new Code();

    /**
     * 开放授权 JWT 配置。
     */
    @Data
    public static class Jwt {

        /** 签名密钥，至少 32 字节 */
        private String secret;

        /** 签发者标识 */
        private String issuer = "deadman-open-auth";
    }

    /**
     * 授权码默认配置（应用未单独配置时使用）。
     */
    @Data
    public static class Code {

        /** Redis key 前缀 */
        private String redisKeyPrefix = OpenAuthConstants.CODE_KEY_PREFIX;
    }
}
