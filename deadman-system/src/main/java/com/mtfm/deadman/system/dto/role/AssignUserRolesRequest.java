package com.mtfm.deadman.system.dto.role;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 为用户分配角色列表（覆盖式）
 * 
 * @param roleIds 角色ID列表
 */
public record AssignUserRolesRequest(@NotNull(message = "角色 ID 列表不能为 null") List<Long> roleIds) {
}
