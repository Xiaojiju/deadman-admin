package com.mtfm.deadman.plugin.pay.permission;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.spi.PermissionContributor;

/**
 * 支付插件权限贡献者。
 */
@Component
@ConditionalOnClass(PermissionContributor.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PayPermissionContributor implements PermissionContributor {

    /**
     * 注册支付相关权限。
     *
     * @return 权限组列表
     */
    @Override
    public List<PermissionGroupDescriptor> contribute() {
        return PayPermissions.permissionGroups();
    }
}
