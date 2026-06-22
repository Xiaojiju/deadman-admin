package com.mtfm.deadman.plugin.file.permission;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.spi.PermissionContributor;

/**
 * 文件管理插件权限贡献者。
 */
@Component
@ConditionalOnClass(PermissionContributor.class)
@ConditionalOnProperty(prefix = "deadman.plugin.file", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FilePermissionContributor implements PermissionContributor {

    /**
     * 注册文件管理相关权限。
     *
     * @return 权限组列表
     */
    @Override
    public List<PermissionGroupDescriptor> contribute() {
        return FilePermissions.permissionGroups();
    }
}
