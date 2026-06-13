package com.mtfm.deadman.support.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 管理端微信桥接配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.support.wechat")
public class AdminWechatSupportProperties {

    /** 是否启用管理端微信桥接 */
    private boolean enabled = true;

    /** 待绑定临时令牌 TTL */
    private Duration bindTokenTtl = Duration.ofMinutes(10);
}
