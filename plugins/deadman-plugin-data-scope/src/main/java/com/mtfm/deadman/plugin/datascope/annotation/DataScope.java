package com.mtfm.deadman.plugin.datascope.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Service/Controller 方法或类启用数据隔离。
 * <p>
 * 仅在被标注的调用链内，MyBatis 查询才会追加数据权限 WHERE 条件；范围类型来自用户 {@code user_data_scope}
 * 配置，不在注解中指定。
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
}
