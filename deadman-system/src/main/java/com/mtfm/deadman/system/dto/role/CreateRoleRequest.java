package com.mtfm.deadman.system.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建角色请求
 * 
 * @param roleCode        角色编码
 * @param roleName        角色名称
 * @param description     角色描述
 * @param permissionCodes 权限码列表
 */
public record CreateRoleRequest(
                @NotBlank(message = "角色编码不能为空") @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "角色编码须为大写字母、数字或下划线，且以字母开头") String roleCode,
                @NotBlank(message = "角色名称不能为空") @Size(max = 64) String roleName,
                @Size(max = 256) String description,
                List<String> permissionCodes) {
}
