package com.mtfm.deadman.support.client.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 用户端微信桥接配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.support.client-wechat")
public class ClientWechatSupportProperties {

    /** 是否启用用户端微信桥接 */
    private boolean enabled = true;

    /** 待绑定临时令牌 TTL */
    private Duration bindTokenTtl = Duration.ofMinutes(10);
}
