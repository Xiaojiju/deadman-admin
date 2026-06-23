package com.mtfm.deadman.support.client.file.autoconfigure;

import com.mtfm.deadman.support.client.file.config.ClientFileSupportProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端 file 桥接自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.mtfm.deadman.plugin.file.service.FileService")
@ConditionalOnProperty(prefix = "deadman.support.client-file", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientFileSupportProperties.class)
@ComponentScan(basePackages = "com.mtfm.deadman.support.client.file")
public class DeadmanSupportClientFileAutoConfiguration {}
