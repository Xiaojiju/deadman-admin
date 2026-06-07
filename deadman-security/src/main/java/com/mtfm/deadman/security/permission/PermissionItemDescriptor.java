package com.mtfm.deadman.security.permission;

/**
 * 权限项描述（注册用）。
 *
 * @param code  权限码，与 {@code @PreAuthorize("hasAuthority('...')")} 一致
 * @param label 展示名称
 */
public record PermissionItemDescriptor(String code, String label) {
}
