package com.mtfm.deadman.support.client.openauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户端开放授权桥接配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.support.client-open-auth")
public class ClientOpenAuthSupportProperties {

    /** 是否启用桥接 */
    private boolean enabled = true;
}
