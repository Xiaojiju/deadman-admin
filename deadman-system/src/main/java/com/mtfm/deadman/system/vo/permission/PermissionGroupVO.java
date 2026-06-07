package com.mtfm.deadman.system.vo.permission;

import java.util.List;

/**
 * 权限功能集及其下属权限码。
 * 
 * @param code        权限组编码
 * @param label       权限组标签
 * @param permissions 权限项列表
 */
public record PermissionGroupVO(String code, String label, List<PermissionItemVO> permissions) {
}
