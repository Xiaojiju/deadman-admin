package com.mtfm.deadman.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验用户/组织状态值仅允许 0（禁用）或 1（正常/启用）。
 */
@Documented
@Constraint(validatedBy = UserStatusValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserStatusValue {

    /** 校验失败提示 */
    String message() default "状态仅支持 0-禁用 或 1-正常";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
