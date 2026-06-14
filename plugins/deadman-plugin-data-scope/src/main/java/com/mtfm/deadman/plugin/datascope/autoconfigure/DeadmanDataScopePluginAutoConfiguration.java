package com.mtfm.deadman.plugin.datascope.autoconfigure;

import com.mtfm.deadman.plugin.datascope.config.DataScopePluginProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据权限插件自动配置。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "deadman.plugin.data-scope", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DataScopePluginProperties.class)
@MapperScan("com.mtfm.deadman.plugin.datascope.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.datascope")
public class DeadmanDataScopePluginAutoConfiguration {
}
