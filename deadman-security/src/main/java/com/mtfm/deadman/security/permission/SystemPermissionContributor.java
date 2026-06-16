package com.mtfm.deadman.security.permission;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.permission.PermissionItemDescriptor;
import com.mtfm.deadman.common.spi.PermissionContributor;
import com.mtfm.deadman.system.permission.SystemPermissions;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统内置模块权限贡献者。
 */
@Component
public class SystemPermissionContributor implements PermissionContributor {

    /**
     * 注册 auth / user / org / role 权限组。
     *
     * @return 权限组列表
     */
    @Override
    public List<PermissionGroupDescriptor> contribute() {
        return List.of(
                new PermissionGroupDescriptor(
                        "auth",
                        "认证与账号",
                        List.of(
                                new PermissionItemDescriptor(
                                        SystemPermissions.Auth.PASSWORD_CHANGE, "修改本人密码"),
                                new PermissionItemDescriptor(
                                        SystemPermissions.Auth.PERMISSIONS_READ, "查看本人权限清单"))),
                new PermissionGroupDescriptor(
                        "user",
                        "用户管理",
                        List.of(
                                new PermissionItemDescriptor(SystemPermissions.User.LIST_READ, "查看用户列表"),
                                new PermissionItemDescriptor(SystemPermissions.User.CREATE, "新增用户"),
                                new PermissionItemDescriptor(SystemPermissions.User.UPDATE, "更新用户资料/状态/头像"),
                                new PermissionItemDescriptor(SystemPermissions.User.DELETE, "删除用户"),
                                new PermissionItemDescriptor(SystemPermissions.User.PASSWORD_RESET, "重置用户密码"),
                                new PermissionItemDescriptor(SystemPermissions.User.PROFILE_READ, "查看本人资料"),
                                new PermissionItemDescriptor(
                                        SystemPermissions.User.PROFILE_UPDATE, "更新本人资料"))),
                new PermissionGroupDescriptor(
                        "org",
                        "组织与职位",
                        List.of(
                                new PermissionItemDescriptor(SystemPermissions.Org.DEPT_LIST_READ, "查看部门"),
                                new PermissionItemDescriptor(SystemPermissions.Org.DEPT_CREATE, "创建部门"),
                                new PermissionItemDescriptor(SystemPermissions.Org.DEPT_UPDATE, "更新部门"),
                                new PermissionItemDescriptor(SystemPermissions.Org.DEPT_DELETE, "删除部门"),
                                new PermissionItemDescriptor(SystemPermissions.Org.POSITION_LIST_READ, "查看职位"),
                                new PermissionItemDescriptor(SystemPermissions.Org.POSITION_CREATE, "创建职位"),
                                new PermissionItemDescriptor(SystemPermissions.Org.POSITION_UPDATE, "更新职位"),
                                new PermissionItemDescriptor(SystemPermissions.Org.POSITION_DELETE, "删除职位"))),
                new PermissionGroupDescriptor(
                        "role",
                        "角色与权限",
                        List.of(
                                new PermissionItemDescriptor(SystemPermissions.Role.LIST_READ, "查看角色列表"),
                                new PermissionItemDescriptor(SystemPermissions.Role.CREATE, "创建角色"),
                                new PermissionItemDescriptor(SystemPermissions.Role.UPDATE, "更新角色"),
                                new PermissionItemDescriptor(SystemPermissions.Role.DELETE, "删除角色"),
                                new PermissionItemDescriptor(
                                        SystemPermissions.Role.PERMISSION_ASSIGN, "分配角色权限"),
                                new PermissionItemDescriptor(SystemPermissions.Role.USER_ASSIGN, "分配用户角色"))));
    }
}
