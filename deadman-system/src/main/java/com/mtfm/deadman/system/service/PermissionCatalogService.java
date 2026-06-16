package com.mtfm.deadman.system.service;

import com.mtfm.deadman.common.spi.PermissionCatalog;
import com.mtfm.deadman.system.vo.permission.PermissionGroupVO;
import com.mtfm.deadman.system.vo.permission.PermissionItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 从运行时权限注册表提供权限目录（只读），供管理端配置角色时使用。
 */
@Service
@RequiredArgsConstructor
public class PermissionCatalogService {

    private final PermissionCatalog permissionCatalog;

    /**
     * 获取全部权限组
     *
     * @return 全部权限组
     */
    public List<PermissionGroupVO> listAllGroups() {
        return permissionCatalog.listPermissionGroups().stream()
                .map(group -> new PermissionGroupVO(
                        group.code(),
                        group.label(),
                        group.permissions().stream()
                                .map(item -> new PermissionItemVO(item.code(), item.label()))
                                .toList()))
                .toList();
    }

    /**
     * 获取全部权限
     *
     * @return 全部权限
     */
    public List<PermissionItemVO> listAllPermissions() {
        return permissionCatalog.listAllPermissionItems().stream()
                .map(item -> new PermissionItemVO(item.code(), item.label()))
                .toList();
    }
}
