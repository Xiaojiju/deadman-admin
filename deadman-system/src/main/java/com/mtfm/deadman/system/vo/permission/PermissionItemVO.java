package com.mtfm.deadman.system.vo.permission;

/**
 * 权限码条目。
 * 
 * @param code  权限码，与 {@code @PreAuthorize("hasAuthority('...')")} 一致
 * @param label 权限码标签
 */
public record PermissionItemVO(String code, String label) {
}
