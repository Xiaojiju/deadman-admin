package com.mtfm.deadman.system.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.system.dto.role.AssignRolePermissionsRequest;
import com.mtfm.deadman.system.dto.role.CreateRoleRequest;
import com.mtfm.deadman.system.dto.role.UpdateRoleRequest;
import com.mtfm.deadman.system.service.RoleAdminService;
import com.mtfm.deadman.system.vo.role.RoleDetailVO;
import com.mtfm.deadman.system.vo.role.RoleSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色与权限分配管理接口。
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleAdminService roleAdminService;

    /**
     * 角色列表
     *
     * @return 角色列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:list:read')")
    public Result<List<RoleSummaryVO>> list() {
        return Result.ok(roleAdminService.listRoles());
    }

    /**
     * 角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:list:read')")
    public Result<RoleDetailVO> detail(@PathVariable Long roleId) {
        return Result.ok(roleAdminService.getRoleDetail(roleId));
    }

    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 创建角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public Result<RoleDetailVO> create(@Valid @RequestBody CreateRoleRequest request) {
        return Result.ok(roleAdminService.createRole(request));
    }

    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 更新角色请求
     * @return 更新角色
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:update')")
    public Result<RoleDetailVO> update(@PathVariable Long roleId, @Valid @RequestBody UpdateRoleRequest request) {
        return Result.ok(roleAdminService.updateRole(roleId, request));
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 删除角色
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<Void> delete(@PathVariable Long roleId) {
        roleAdminService.deleteRole(roleId);
        return Result.ok();
    }

    /**
     * 分配角色权限
     *
     * @param roleId  角色ID
     * @param request 分配角色权限请求
     * @return 分配角色权限
     */
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:permission:assign')")
    public Result<RoleDetailVO> assignPermissions(
            @PathVariable Long roleId, @Valid @RequestBody AssignRolePermissionsRequest request) {
        return Result.ok(roleAdminService.assignRolePermissions(roleId, request));
    }
}
