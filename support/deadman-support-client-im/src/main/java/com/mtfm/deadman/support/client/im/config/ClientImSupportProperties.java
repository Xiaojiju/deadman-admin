package com.mtfm.deadman.support.client.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户端 IM 桥接配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.support.client-im")
public class ClientImSupportProperties {

    /** 是否启用用户端 IM 桥接 */
    private boolean enabled = true;
}
