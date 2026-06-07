package com.mtfm.deadman.security.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.security.service.PermissionCatalogService;
import com.mtfm.deadman.system.vo.permission.PermissionGroupVO;
import com.mtfm.deadman.system.vo.permission.PermissionItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限码目录（注册表只读，非菜单）。
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionCatalogService permissionCatalogService;

    /**
     * 权限码目录（按分组）
     *
     * @return 权限码目录（按分组）
     */
    @GetMapping("/catalog")
    @PreAuthorize("hasAuthority('role:list:read')")
    public Result<List<PermissionGroupVO>> catalog() {
        return Result.ok(permissionCatalogService.listAllGroups());
    }

    /**
     * 权限码列表（平铺）
     *
     * @return 权限码列表（平铺）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:list:read')")
    public Result<List<PermissionItemVO>> listFlat() {
        return Result.ok(permissionCatalogService.listAllPermissions());
    }
}
