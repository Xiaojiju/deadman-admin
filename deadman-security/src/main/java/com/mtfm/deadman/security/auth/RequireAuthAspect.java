package com.mtfm.deadman.security.auth;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * {@link com.mtfm.deadman.common.auth.RequireAuth} 切面：在 Controller 方法执行前校验登录态与认证域。
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class RequireAuthAspect {

    private final AuthRealmPrincipalMatcherRegistry matcherRegistry;

    /**
     * 校验标注了 {@link com.mtfm.deadman.common.auth.RequireAuth} 的 Controller 方法。
     *
     * @param joinPoint 切点
     */
    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void checkRequireAuth(JoinPoint joinPoint) {
        AuthRealm requiredRealm = RequireAuthAnnotationSupport.resolveRequiredRealm(joinPoint);
        if (requiredRealm == null) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!matcherRegistry.matches(requiredRealm, principal)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }
}
