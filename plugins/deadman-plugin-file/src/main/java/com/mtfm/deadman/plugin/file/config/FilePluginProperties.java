package com.mtfm.deadman.plugin.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * 文件管理插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.file")
public class FilePluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** 默认存储 Provider 标识 */
    private String defaultProvider = "local";

    /** 单文件最大大小 */
    private DataSize maxFileSize = DataSize.ofMegabytes(10);
}
