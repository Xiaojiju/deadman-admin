package com.mtfm.deadman.plugin.datascope.aspect;

import com.mtfm.deadman.plugin.datascope.context.DataScopeContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * {@link com.mtfm.deadman.plugin.datascope.annotation.DataScope}
 * 切面，在标注方法执行期间启用数据隔离。
 */
@Aspect
@Component
@Order(100)
public class DataScopeAspect {

    /**
     * 对标注了 {@code @DataScope} 的方法或类，启用当前线程数据隔离上下文。
     *
     * @param joinPoint 切点
     * @return 原方法返回值
     * @throws Throwable 原方法抛出的异常
     */
    @Around("@annotation(com.mtfm.deadman.plugin.datascope.annotation.DataScope) "
            + "|| @within(com.mtfm.deadman.plugin.datascope.annotation.DataScope)")
    public Object aroundScoped(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean alreadyEnabled = DataScopeContextHolder.isEnabled();
        if (!alreadyEnabled) {
            DataScopeContextHolder.enable();
        }
        try {
            return joinPoint.proceed();
        } finally {
            if (!alreadyEnabled) {
                DataScopeContextHolder.clearEnabled();
            }
        }
    }
}
