package com.mtfm.deadman.plugin.file.autoconfigure;

import com.mtfm.deadman.plugin.file.config.FilePluginProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 文件管理插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(FilePluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.file", name = "enabled", havingValue = "true", matchIfMissing = true)
@MapperScan("com.mtfm.deadman.plugin.file.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.file")
public class DeadmanFilePluginAutoConfiguration {}
