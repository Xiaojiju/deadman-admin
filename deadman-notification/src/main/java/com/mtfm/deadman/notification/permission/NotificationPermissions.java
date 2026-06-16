package com.mtfm.deadman.notification.permission;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.permission.PermissionItemDescriptor;

import java.util.List;

/**
 * 站内信模块权限码常量与注册定义。
 */
public final class NotificationPermissions {

    public static final String SEND = "notification:send";
    public static final String SENT_READ = "notification:sent:read";
    public static final String INBOX_READ = "notification:inbox:read";
    public static final String INBOX_UPDATE = "notification:inbox:update";

    private NotificationPermissions() {
    }

    /**
     * 站内信权限组定义。
     *
     * @return 权限组
     */
    public static List<PermissionGroupDescriptor> permissionGroups() {
        return List.of(new PermissionGroupDescriptor(
                "notification",
                "站内信通知",
                List.of(
                        new PermissionItemDescriptor(SEND, "发送站内信"),
                        new PermissionItemDescriptor(SENT_READ, "查看已发送站内信"),
                        new PermissionItemDescriptor(INBOX_READ, "查看本人收件箱"),
                        new PermissionItemDescriptor(INBOX_UPDATE, "标记站内信已读"))));
    }
}
