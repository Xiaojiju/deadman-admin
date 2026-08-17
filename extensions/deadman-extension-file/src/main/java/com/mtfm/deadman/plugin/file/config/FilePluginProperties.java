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

    /**
     * 公网访问基址（如 {@code https://api.example.com}）。
     * <p>本地存储返回相对路径 {@code /files/...} 时，换链会拼成绝对 URL，供 IM FaceUrl 等外部系统使用。
     * COS/OSS 已返回 {@code https://} 时本配置不生效。
     */
    private String publicBaseUrl;
}
