package com.mtfm.deadman.notification.permission;

import com.mtfm.deadman.security.permission.PermissionContributor;
import com.mtfm.deadman.security.permission.PermissionGroupDescriptor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 站内信模块权限贡献者。
 */
@Component
public class NotificationPermissionContributor implements PermissionContributor {

    /**
     * 注册站内信相关权限。
     *
     * @return 权限组列表
     */
    @Override
    public List<PermissionGroupDescriptor> contribute() {
        return NotificationPermissions.permissionGroups();
    }
}
