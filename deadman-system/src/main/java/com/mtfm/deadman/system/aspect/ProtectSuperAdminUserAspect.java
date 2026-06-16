package com.mtfm.deadman.system.aspect;

import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 超级管理员保护切面：拦截标注 {@link ProtectSuperAdminUser} 的方法。
 */
@Aspect
@Component
@Order(50)
@RequiredArgsConstructor
public class ProtectSuperAdminUserAspect {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 在目标方法执行前校验用户不是超级管理员。
     *
     * @param joinPoint 切点
     * @param protect   注解配置
     */
    @Before("@annotation(protect)")
    public void beforeProtectedOperation(JoinPoint joinPoint, ProtectSuperAdminUser protect) {
        if (StringUtils.hasText(protect.condition()) && !matchesCondition(joinPoint, protect.condition())) {
            return;
        }
        Long userId = resolveUserId(joinPoint, protect.userIdParam());
        if (userId == null) {
            return;
        }
        if (sysUserRoleMapper.selectRoleCodesByUserId(userId).contains(SysRoleCodes.SUPER_ADMIN)) {
            throw new BusinessException(ResultCode.USER_SUPER_ADMIN_PROTECTED);
        }
    }

    private boolean matchesCondition(JoinPoint joinPoint, String condition) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                null, signature.getMethod(), joinPoint.getArgs(), parameterNameDiscoverer);
        Boolean matched = expressionParser.parseExpression(condition).getValue(context, Boolean.class);
        return Boolean.TRUE.equals(matched);
    }

    private Long resolveUserId(JoinPoint joinPoint, String userIdParam) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (paramNames == null) {
            return null;
        }
        for (int i = 0; i < paramNames.length; i++) {
            if (userIdParam.equals(paramNames[i]) && args[i] instanceof Long userId) {
                return userId;
            }
        }
        return null;
    }
}
