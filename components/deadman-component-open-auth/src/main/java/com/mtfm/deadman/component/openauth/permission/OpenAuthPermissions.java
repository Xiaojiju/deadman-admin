package com.mtfm.deadman.component.openauth.permission;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.permission.PermissionItemDescriptor;

import java.util.List;

/**
 * 开放授权组件权限码常量。
 */
public final class OpenAuthPermissions {

    /** 功能集编码 */
    public static final String GROUP_CODE = "open-app";

    public static final String LIST_READ = "open-app:list:read";
    public static final String CREATE = "open-app:create";
    public static final String UPDATE = "open-app:update";
    public static final String DELETE = "open-app:delete";
    public static final String SECRET_ROTATE = "open-app:secret:rotate";

    private OpenAuthPermissions() {
    }

    /**
     * 开放授权权限组定义。
     *
     * @return 权限组
     */
    public static List<PermissionGroupDescriptor> permissionGroups() {
        return List.of(new PermissionGroupDescriptor(
                GROUP_CODE,
                "开放应用管理",
                List.of(
                        new PermissionItemDescriptor(LIST_READ, "查看开放应用列表"),
                        new PermissionItemDescriptor(CREATE, "创建开放应用"),
                        new PermissionItemDescriptor(UPDATE, "更新开放应用"),
                        new PermissionItemDescriptor(DELETE, "删除开放应用"),
                        new PermissionItemDescriptor(SECRET_ROTATE, "轮换应用密钥"))));
    }
}
