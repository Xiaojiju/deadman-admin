package com.mtfm.deadman.plugin.pay.permission;

import java.util.List;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.permission.PermissionItemDescriptor;

/**
 * 支付插件权限码（转账额度与派发管理）。
 */
public final class PayPermissions {

    /** 功能集编码 */
    public static final String GROUP_CODE = "pay";

    public static final String TRANSFER_QUOTA_READ = "pay:transfer:quota:read";
    public static final String TRANSFER_QUOTA_WRITE = "pay:transfer:quota:write";
    public static final String TRANSFER_CREATE = "pay:transfer:create";
    public static final String TRANSFER_READ = "pay:transfer:read";
    public static final String TRANSFER_DISPATCH = "pay:transfer:dispatch";

    private PayPermissions() {
    }

    /**
     * 支付权限组定义。
     *
     * @return 权限组
     */
    public static List<PermissionGroupDescriptor> permissionGroups() {
        return List.of(new PermissionGroupDescriptor(
                GROUP_CODE,
                "支付管理",
                List.of(
                        new PermissionItemDescriptor(TRANSFER_QUOTA_READ, "查看转账额度"),
                        new PermissionItemDescriptor(TRANSFER_QUOTA_WRITE, "配置转账额度"),
                        new PermissionItemDescriptor(TRANSFER_CREATE, "发起商家转账"),
                        new PermissionItemDescriptor(TRANSFER_READ, "查看转账订单"),
                        new PermissionItemDescriptor(TRANSFER_DISPATCH, "派发/停止待转"))));
    }
}
