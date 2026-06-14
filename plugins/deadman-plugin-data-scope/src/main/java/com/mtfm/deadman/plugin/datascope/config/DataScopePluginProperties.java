package com.mtfm.deadman.plugin.datascope.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据权限插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.data-scope")
public class DataScopePluginProperties {

    /** 是否启用数据权限插件 */
    private boolean enabled = true;

    /**
     * 表级过滤列映射（可选兜底），key 为物理表名（小写）；优先使用 Mapper
     * {@link com.mtfm.deadman.plugin.datascope.annotation.DataColumn}
     */
    private Map<String, TableColumnMapping> columns = new LinkedHashMap<>();

    /**
     * 单表列映射配置。
     */
    @Data
    public static class TableColumnMapping {

        /** 部门列名 */
        private String deptColumn;

        /** 用户列名 */
        private String userColumn;
    }
}
