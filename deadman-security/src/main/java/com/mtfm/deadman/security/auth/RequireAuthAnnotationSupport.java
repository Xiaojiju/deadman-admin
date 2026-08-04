package com.mtfm.deadman.security.auth;

import com.mtfm.deadman.common.auth.AllowAnonymous;
import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 解析 Controller 方法上的 {@link RequireAuth} / {@link AllowAnonymous} 注解。
 */
public final class RequireAuthAnnotationSupport {

    private RequireAuthAnnotationSupport() {
    }

    /**
     * 解析当前切点是否需要认证及所属域。
     * <p>
     * 方法级 {@link AllowAnonymous} 优先；其次方法级 {@link RequireAuth}；最后类级 {@link RequireAuth}。
     *
     * @param joinPoint AOP 切点
     * @return 需要的认证域，无需强制登录时返回 null
     */
    public static AuthRealm resolveRequiredRealm(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(AllowAnonymous.class)) {
            return null;
        }
        RequireAuth methodAuth = method.getAnnotation(RequireAuth.class);
        if (methodAuth != null) {
            return methodAuth.value();
        }
        RequireAuth typeAuth = joinPoint.getTarget().getClass().getAnnotation(RequireAuth.class);
        if (typeAuth != null) {
            return typeAuth.value();
        }
        return null;
    }
}
