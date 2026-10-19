package com.mtfm.deadman.support.client.sms.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.component.client.autoconfigure.DeadmanClientComponentAutoConfiguration;
import com.mtfm.deadman.plugin.smslogin.autoconfigure.DeadmanSmsLoginPluginAutoConfiguration;

/**
 * 用户端短信登录桥接。在用户端组件与短信登录插件同时启用时装配。
 */
@AutoConfiguration
@AutoConfigureAfter({DeadmanClientComponentAutoConfiguration.class, DeadmanSmsLoginPluginAutoConfiguration.class})
@ConditionalOnClass(name = "com.mtfm.deadman.component.client.service.ClientUserService")
@ConditionalOnProperty(prefix = "deadman.component.client", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@ComponentScan(basePackages = "com.mtfm.deadman.support.client.sms")
public class DeadmanClientSmsSupportAutoConfiguration {}
