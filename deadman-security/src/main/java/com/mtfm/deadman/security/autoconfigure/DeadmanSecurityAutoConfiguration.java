package com.mtfm.deadman.security.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证与授权模块自动配置。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.mtfm.deadman.security")
public class DeadmanSecurityAutoConfiguration {
}
