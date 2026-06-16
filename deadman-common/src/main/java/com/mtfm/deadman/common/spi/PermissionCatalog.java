package com.mtfm.deadman.common.spi;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.permission.PermissionItemDescriptor;

import java.util.List;
import java.util.Set;

/**
 * 已注册权限目录只读 SPI，由 security 模块的 {@code PermissionRegistry} 聚合实现。
 */
public interface PermissionCatalog {

    /**
     * 全部已注册权限码。
     *
     * @return 权限码集合
     */
    Set<String> allPermissionCodes();

    /**
     * 判断权限码是否已注册。
     *
     * @param code 权限码
     * @return 是否有效
     */
    boolean isValidPermissionCode(String code);

    /**
     * 按功能集列出全部权限。
     *
     * @return 权限组列表
     */
    List<PermissionGroupDescriptor> listPermissionGroups();

    /**
     * 扁平列出全部权限项。
     *
     * @return 权限项列表
     */
    List<PermissionItemDescriptor> listAllPermissionItems();
}
