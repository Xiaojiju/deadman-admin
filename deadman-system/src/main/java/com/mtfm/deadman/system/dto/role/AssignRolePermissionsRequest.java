package com.mtfm.deadman.system.dto.role;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 为角色分配权限码。
 * 
 * @param permissionCodes 权限码列表
 */
public record AssignRolePermissionsRequest(@NotNull(message = "权限码列表不能为 null") List<String> permissionCodes) {
}
