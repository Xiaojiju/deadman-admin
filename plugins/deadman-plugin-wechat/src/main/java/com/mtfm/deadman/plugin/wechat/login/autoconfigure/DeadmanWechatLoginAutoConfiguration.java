package com.mtfm.deadman.plugin.wechat.login.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 微信登录统一门面自动配置。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.wechat.login")
public class DeadmanWechatLoginAutoConfiguration {
}
