package com.mtfm.deadman.system.dto.role;

import jakarta.validation.constraints.Size;

/**
 * 更新角色请求（不含角色编码）
 * 
 * @param roleName    角色名称
 * @param description 角色描述
 * @param status      角色状态
 */
public record UpdateRoleRequest(@Size(max = 64) String roleName, @Size(max = 256) String description, Integer status) {
}
