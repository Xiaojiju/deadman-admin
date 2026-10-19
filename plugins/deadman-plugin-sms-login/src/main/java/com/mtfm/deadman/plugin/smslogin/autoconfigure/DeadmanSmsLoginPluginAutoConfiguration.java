package com.mtfm.deadman.plugin.smslogin.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.plugin.sms.autoconfigure.DeadmanSmsPluginAutoConfiguration;
import com.mtfm.deadman.plugin.smslogin.config.SmsLoginPluginProperties;

/**
 * 短信验证码登录插件自动配置。
 */
@AutoConfiguration
@AutoConfigureAfter(DeadmanSmsPluginAutoConfiguration.class)
@EnableConfigurationProperties(SmsLoginPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.sms-login", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.smslogin")
public class DeadmanSmsLoginPluginAutoConfiguration {}
