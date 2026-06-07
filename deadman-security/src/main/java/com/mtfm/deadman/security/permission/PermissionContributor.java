package com.mtfm.deadman.security.permission;

import java.util.List;

/**
 * 权限贡献者 SPI：各业务模块或组件在启动时注册本模块权限定义。
 * <p>
 * 实现类注册为 Spring Bean 后由 {@link PermissionRegistry} 自动聚合。
 */
public interface PermissionContributor {

    /**
     * 贡献权限功能集及权限项。
     *
     * @return 权限组列表，可为空
     */
    List<PermissionGroupDescriptor> contribute();
}
