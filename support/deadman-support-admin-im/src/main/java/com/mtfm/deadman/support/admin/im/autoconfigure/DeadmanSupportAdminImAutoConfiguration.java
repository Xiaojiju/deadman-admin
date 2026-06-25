package com.mtfm.deadman.support.admin.im.autoconfigure;

import com.mtfm.deadman.support.admin.im.config.AdminImSupportProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 管理端 IM 桥接自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.mtfm.deadman.plugin.im.tencent.service.ImService")
@ConditionalOnProperty(prefix = "deadman.support.admin-im", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AdminImSupportProperties.class)
@ComponentScan(basePackages = "com.mtfm.deadman.support.admin.im")
public class DeadmanSupportAdminImAutoConfiguration {}
