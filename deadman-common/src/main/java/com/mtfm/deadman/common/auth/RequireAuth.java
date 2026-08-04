package com.mtfm.deadman.common.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明接口必须已登录且属于指定认证域。
 * <p>
 * 可标注在类或方法上；方法级优先于类级。未标注且未在类上声明的接口允许匿名访问
 * （若需在匿名与已登录两种状态下分支处理，请使用各域的 {@code *AuthSupport} 工具类）。
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    /**
     * 要求的认证域。
     *
     * @return 认证域
     */
    AuthRealm value();
}
