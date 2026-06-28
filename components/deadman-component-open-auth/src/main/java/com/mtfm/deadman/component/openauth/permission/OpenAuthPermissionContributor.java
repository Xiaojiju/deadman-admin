package com.mtfm.deadman.component.openauth.permission;

import com.mtfm.deadman.common.spi.PermissionContributor;
import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 开放授权组件权限贡献者。
 */
@Component
@ConditionalOnProperty(prefix = "deadman.component.open-auth", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenAuthPermissionContributor implements PermissionContributor {

    /**
     * 注册开放应用管理权限。
     *
     * @return 权限组列表
     */
    @Override
    public List<PermissionGroupDescriptor> contribute() {
        return OpenAuthPermissions.permissionGroups();
    }
}
