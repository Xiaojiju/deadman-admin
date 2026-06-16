package com.mtfm.deadman.common.util;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 控制器层「要求已登录」辅助方法。
 */
public final class AuthPrincipalSupport {

    private AuthPrincipalSupport() {
    }

    /**
     * 要求 principal 非空，否则抛出未认证业务异常。
     *
     * @param principal 当前登录主体
     * @param <T>       主体类型
     * @return 非空主体
     */
    public static <T> T requireAuthenticated(T principal) {
        if (principal == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return principal;
    }
}
