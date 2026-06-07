package com.mtfm.deadman.plugin.storage.local.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地磁盘存储插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.storage-local")
public class LocalStoragePluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** 文件存储根目录（绝对或相对路径） */
    private String basePath = "./data/files";

    /** 公开访问 URL 前缀，映射后可通过浏览器直接访问 */
    private String publicUrlPrefix = "/files";
}
