package com.mtfm.deadman.common.permission;

import java.util.List;

/**
 * 权限功能集描述（注册用），供管理端权限目录展示。
 *
 * @param code        功能集编码
 * @param label       功能集名称
 * @param permissions 本组权限项
 */
public record PermissionGroupDescriptor(String code, String label, List<PermissionItemDescriptor> permissions) {
}
