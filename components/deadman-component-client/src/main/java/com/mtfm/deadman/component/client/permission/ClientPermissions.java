package com.mtfm.deadman.component.client.permission;

import com.mtfm.deadman.security.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.security.permission.PermissionItemDescriptor;

import java.util.List;

/**
 * 用户端组件权限码常量与注册定义。
 */
public final class ClientPermissions {

    /** 功能集编码 */
    public static final String GROUP_CODE = "client-user";

    public static final String LIST_READ = "client-user:list:read";
    public static final String UPDATE = "client-user:update";
    public static final String DELETE = "client-user:delete";

    private ClientPermissions() {
    }

    /**
     * 用户端组件权限组定义。
     *
     * @return 权限组
     */
    public static List<PermissionGroupDescriptor> permissionGroups() {
        return List.of(new PermissionGroupDescriptor(
                GROUP_CODE,
                "用户端用户管理",
                List.of(
                        new PermissionItemDescriptor(LIST_READ, "查看用户端用户列表"),
                        new PermissionItemDescriptor(UPDATE, "禁用用户端用户"),
                        new PermissionItemDescriptor(DELETE, "注销用户端用户"))));
    }
}
