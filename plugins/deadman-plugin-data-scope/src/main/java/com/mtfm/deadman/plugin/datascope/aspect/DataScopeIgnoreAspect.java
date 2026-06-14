package com.mtfm.deadman.plugin.datascope.aspect;

import com.mtfm.deadman.plugin.datascope.context.DataScopeContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * {@link com.mtfm.deadman.plugin.datascope.annotation.DataScopeIgnore} 切面，在方法执行期间跳过 SQL 数据权限拼接。
 */
@Aspect
@Component
@Order(50)
public class DataScopeIgnoreAspect {

    /**
     * 对标注了忽略注解的方法或类，临时关闭当前线程的数据隔离。
     *
     * @param joinPoint 切点
     * @return 原方法返回值
     * @throws Throwable 原方法抛出的异常
     */
    @Around("@annotation(com.mtfm.deadman.plugin.datascope.annotation.DataScopeIgnore) "
            + "|| @within(com.mtfm.deadman.plugin.datascope.annotation.DataScopeIgnore)")
    public Object aroundIgnored(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean alreadyIgnored = DataScopeContextHolder.isIgnored();
        if (!alreadyIgnored) {
            DataScopeContextHolder.ignore();
        }
        try {
            return joinPoint.proceed();
        } finally {
            if (!alreadyIgnored) {
                DataScopeContextHolder.clearIgnored();
            }
        }
    }
}
