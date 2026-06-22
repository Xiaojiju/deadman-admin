package com.mtfm.deadman.support.client.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户端 file 桥接模块配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.support.client-file")
public class ClientFileSupportProperties {

    /** 是否启用 C 端文件上传桥接 */
    private boolean enabled = true;
}
