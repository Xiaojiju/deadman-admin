package com.mtfm.deadman.support.admin.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 管理端 IM 桥接配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.support.admin-im")
public class AdminImSupportProperties {

    /** 是否启用管理端 IM 桥接 */
    private boolean enabled = true;
}
