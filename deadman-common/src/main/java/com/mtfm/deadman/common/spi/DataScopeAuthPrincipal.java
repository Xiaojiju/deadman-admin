package com.mtfm.deadman.common.spi;

/**
 * 数据权限插件可识别的认证主体，由 security 模块 {@code LoginUser} 实现。
 */
public interface DataScopeAuthPrincipal {

    /**
     * @return 用户 ID
     */
    Long userId();

    /**
     * @return 是否超级管理员（跳过数据隔离）
     */
    boolean superAdmin();
}
