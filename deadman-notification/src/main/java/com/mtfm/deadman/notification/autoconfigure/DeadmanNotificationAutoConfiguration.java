package com.mtfm.deadman.notification.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 站内信通知模块自动配置。
 */
@AutoConfiguration
@MapperScan("com.mtfm.deadman.notification.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.notification")
public class DeadmanNotificationAutoConfiguration {
}
