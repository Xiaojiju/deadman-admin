package com.mtfm.deadman.system.vo.role;

import java.util.List;

/**
 * 角色详情（含权限码列表）。
 * 
 * @param id              角色主键{@link SysRole#id}
 * @param roleCode        角色编码{@link SysRole#roleCode}
 * @param roleName        角色名称{@link SysRole#roleName}
 * @param description     角色描述{@link SysRole#description}
 * @param status          角色状态{@link SysRole#status}
 * @param systemBuiltin   是否系统内置{@link SysRole#systemBuiltin}
 * @param permissionCodes 权限码列表
 */
public record RoleDetailVO(
                Long id,
                String roleCode,
                String roleName,
                String description,
                Integer status,
                boolean systemBuiltin,
                List<String> permissionCodes) {
}
