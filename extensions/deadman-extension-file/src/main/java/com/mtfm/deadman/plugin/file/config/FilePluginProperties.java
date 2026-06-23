package com.mtfm.deadman.plugin.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import lombok.Data;

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

    /** 是否强制校验 bizType 已在注册表中登记 */
    private boolean bizTypeStrict = true;
}
