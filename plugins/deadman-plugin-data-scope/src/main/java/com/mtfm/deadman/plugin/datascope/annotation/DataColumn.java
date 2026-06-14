package com.mtfm.deadman.plugin.datascope.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明 Mapper 对应表的部门/用户过滤列。
 * <p>
 * 未指定 {@link #table()} 时，从 {@code BaseMapper} 泛型实体的 {@code @TableName} 推导物理表名。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataColumn {

    /** 部门 ID 列名 */
    String dept() default "";

    /** 用户 ID 列名（SELF 范围时使用） */
    String user() default "";

    /** 物理表名；为空时从实体 {@code @TableName} 推导 */
    String table() default "";
}
