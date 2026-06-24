package com.mtfm.deadman.plugin.logistics.permission;

import java.util.List;

import com.mtfm.deadman.common.permission.PermissionGroupDescriptor;
import com.mtfm.deadman.common.permission.PermissionItemDescriptor;

/**
 * 物流能力权限码常量与注册定义。
 */
public final class LogisticsPermissions {

    /** 功能集编码 */
    public static final String GROUP_CODE = "logistics";

    public static final String TRACK_QUERY = "logistics:track:query";
    public static final String CARRIER_DETECT = "logistics:carrier:detect";
    public static final String TRACK_SUBSCRIBE = "logistics:track:subscribe";
    public static final String WAYBILL_CREATE = "logistics:waybill:create";
    public static final String WAYBILL_CANCEL = "logistics:waybill:cancel";
    public static final String SHIP_MERCHANT_CREATE = "logistics:ship:merchant:create";
    public static final String SHIP_MERCHANT_CANCEL = "logistics:ship:merchant:cancel";
    public static final String SHIP_CONSUMER_CREATE = "logistics:ship:consumer:create";
    public static final String SHIP_CONSUMER_CANCEL = "logistics:ship:consumer:cancel";
    public static final String SHIP_CONSUMER_PRICE = "logistics:ship:consumer:price";

    private LogisticsPermissions() {
    }

    /**
     * 物流能力权限组定义。
     *
     * @return 权限组
     */
    public static List<PermissionGroupDescriptor> permissionGroups() {
        return List.of(new PermissionGroupDescriptor(
                GROUP_CODE,
                "物流能力",
                List.of(
                        new PermissionItemDescriptor(TRACK_QUERY, "查询快递轨迹"),
                        new PermissionItemDescriptor(CARRIER_DETECT, "智能识别快递公司"),
                        new PermissionItemDescriptor(TRACK_SUBSCRIBE, "订阅轨迹推送"),
                        new PermissionItemDescriptor(WAYBILL_CREATE, "电子面单下单"),
                        new PermissionItemDescriptor(WAYBILL_CANCEL, "取消电子面单"),
                        new PermissionItemDescriptor(SHIP_MERCHANT_CREATE, "商家寄件下单"),
                        new PermissionItemDescriptor(SHIP_MERCHANT_CANCEL, "取消商家寄件"),
                        new PermissionItemDescriptor(SHIP_CONSUMER_CREATE, "C 端寄件下单"),
                        new PermissionItemDescriptor(SHIP_CONSUMER_CANCEL, "取消 C 端寄件"),
                        new PermissionItemDescriptor(SHIP_CONSUMER_PRICE, "C 端寄件询价"))));
    }
}
