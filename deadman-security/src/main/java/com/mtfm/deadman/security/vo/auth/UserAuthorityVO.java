package com.mtfm.deadman.security.vo.auth;

import java.util.List;

/**
 * 当前用户角色与权限码清单。
 * <p>
 * 用户绑定多角色时，角色编码与权限码均会去重合并。
 * 
 * @param roleCodes       角色编码列表{@link SysRole#code}
 * @param permissionCodes 权限码列表，见 {@link com.mtfm.deadman.common.spi.PermissionCatalog} 注册项
 * @param superAdmin      是否超级管理员{@link SysRoleCodes#SUPER_ADMIN}
 */
public record UserAuthorityVO(List<String> roleCodes, List<String> permissionCodes, boolean superAdmin) {
}
