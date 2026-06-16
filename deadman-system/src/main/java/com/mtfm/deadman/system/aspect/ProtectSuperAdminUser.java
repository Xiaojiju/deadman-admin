package com.mtfm.deadman.system.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需拦截超级管理员保护规则的方法（如删除、停用）。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtectSuperAdminUser {

    /**
     * 方法参数名，用于提取目标用户 ID。
     */
    String userIdParam() default "userId";

    /**
     * SpEL 条件，为空表示始终校验；为 true 时才执行保护。
     * <p>
     * 示例：{@code #request.status() != null && #request.status() == 0}
     */
    String condition() default "";
}
