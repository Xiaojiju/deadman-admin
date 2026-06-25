package com.mtfm.deadman.support.client.im.autoconfigure;

import com.mtfm.deadman.support.client.im.config.ClientImSupportProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端 IM 桥接自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.mtfm.deadman.plugin.im.tencent.service.ImService")
@ConditionalOnProperty(prefix = "deadman.support.client-im", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientImSupportProperties.class)
@ComponentScan(basePackages = "com.mtfm.deadman.support.client.im")
public class DeadmanSupportClientImAutoConfiguration {}
