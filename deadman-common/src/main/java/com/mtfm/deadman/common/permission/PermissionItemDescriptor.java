package com.mtfm.deadman.common.permission;

/**
 * 单个权限项描述（注册用）。
 *
 * @param code  权限码
 * @param label 权限名称
 */
public record PermissionItemDescriptor(String code, String label) {
}
