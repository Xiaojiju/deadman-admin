package com.mtfm.deadman.plugin.excel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Excel 工具包插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.excel")
public class ExcelPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;
}
