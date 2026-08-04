package com.mtfm.deadman.common.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 允许匿名访问，用于覆盖类级 {@link RequireAuth}。
 * <p>
 * 仅标注在方法上；与 {@link RequireAuth} 同时标注在方法上时，以 {@link AllowAnonymous} 为准。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowAnonymous {
}
