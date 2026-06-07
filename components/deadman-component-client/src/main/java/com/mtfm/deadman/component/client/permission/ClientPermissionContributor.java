package com.mtfm.deadman.component.client.permission;

import com.mtfm.deadman.security.permission.PermissionContributor;
import com.mtfm.deadman.security.permission.PermissionGroupDescriptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户端组件权限贡献者。
 */
@Component
@ConditionalOnProperty(prefix = "deadman.component.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClientPermissionContributor implements PermissionContributor {

    /**
     * 注册用户端用户管理相关权限。
     *
     * @return 权限组列表
     */
    @Override
    public List<PermissionGroupDescriptor> contribute() {
        return ClientPermissions.permissionGroups();
    }
}
