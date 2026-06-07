package com.mtfm.deadman.plugin.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 列映射注解，标注在 POJO 字段或 Record 组件上。
 * <p>
 * 未标注任何列时，默认导出/导入全部字段（Record 组件或 POJO 非静态字段），表头为字段名。
 */
@Documented
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeadExcelColumn {

    /**
     * 表头名称，默认使用字段名。
     */
    String value() default "";

    /**
     * 列序号（从 0 开始），未指定时按声明顺序排列。
     */
    int index() default -1;

    /**
     * 列宽（字符数），导出时生效。
     */
    int width() default 20;

    /**
     * 日期/时间格式化模式，如 {@code yyyy-MM-dd HH:mm:ss}。
     */
    String dateFormat() default "";

    /**
     * 是否忽略该列，不参与导入导出。
     */
    boolean ignore() default false;
}
