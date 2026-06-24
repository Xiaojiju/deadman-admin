package com.mtfm.deadman.plugin.logistics.permission;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.spi.PermissionContributor;

/**
 * 物流查单权限贡献者。
 */
@Component
@ConditionalOnClass(PermissionContributor.class)
@ConditionalOnProperty(prefix = "deadman.plugin.logistics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogisticsPermissionContributor implements PermissionContributor {

    /**
     * 注册物流查单相关权限。
     *
     * @return 权限组列表
     */
    @Override
    public List<PermissionGroupDescriptor> contribute() {
        return LogisticsPermissions.permissionGroups();
    }
}
