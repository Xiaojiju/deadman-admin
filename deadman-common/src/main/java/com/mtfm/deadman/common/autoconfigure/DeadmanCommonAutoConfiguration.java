package com.mtfm.deadman.common.autoconfigure;

import com.mtfm.deadman.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 公共模块自动配置：全局异常处理等。
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class DeadmanCommonAutoConfiguration {
}
