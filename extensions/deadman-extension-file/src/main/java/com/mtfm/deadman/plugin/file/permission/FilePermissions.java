package com.mtfm.deadman.plugin.file.permission;

import java.util.List;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.permission.PermissionItemDescriptor;

/**
 * 文件管理权限码常量与注册定义。
 */
public final class FilePermissions {

    /** 功能集编码 */
    public static final String GROUP_CODE = "file";

    public static final String UPLOAD = "file:upload";
    public static final String DOWNLOAD = "file:download";
    public static final String READ = "file:read";
    public static final String DELETE = "file:delete";

    private FilePermissions() {
    }

    /**
     * 文件管理权限组定义。
     *
     * @return 权限组
     */
    public static List<PermissionGroupDescriptor> permissionGroups() {
        return List.of(new PermissionGroupDescriptor(
                GROUP_CODE,
                "文件管理",
                List.of(
                        new PermissionItemDescriptor(UPLOAD, "上传文件"),
                        new PermissionItemDescriptor(DOWNLOAD, "下载文件"),
                        new PermissionItemDescriptor(READ, "查看文件信息"),
                        new PermissionItemDescriptor(DELETE, "删除文件"))));
    }
}
