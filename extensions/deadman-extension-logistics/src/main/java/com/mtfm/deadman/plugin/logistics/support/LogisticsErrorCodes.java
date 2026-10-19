package com.mtfm.deadman.plugin.logistics.support;

/**
 * 物流模块业务码。数值与历史接口保持一致，文案键仍为 result.LOGISTICS_*。
 */
public final class LogisticsErrorCodes {

    /** 物流 Provider 不存在 */
    public static final int LOGISTICS_PROVIDER_NOT_FOUND = 14201;

    /** 快递轨迹查询失败 */
    public static final int LOGISTICS_TRACK_QUERY_FAILED = 14202;

    /** 物流插件配置无效 */
    public static final int LOGISTICS_CONFIG_INVALID = 14203;

    /** 快递公司识别失败 */
    public static final int LOGISTICS_CARRIER_DETECT_FAILED = 14204;

    /** 快递轨迹订阅失败 */
    public static final int LOGISTICS_SUBSCRIBE_FAILED = 14205;

    /** 订阅推送验签失败 */
    public static final int LOGISTICS_SUBSCRIBE_PUSH_INVALID = 14206;

    /** 电子面单操作失败 */
    public static final int LOGISTICS_WAYBILL_FAILED = 14207;

    /** 寄件下单失败 */
    public static final int LOGISTICS_SHIP_ORDER_FAILED = 14208;

    /** 寄件取消失败 */
    public static final int LOGISTICS_SHIP_CANCEL_FAILED = 14209;

    /** 快递公司编码未注册或不支持当前渠道 */
    public static final int LOGISTICS_CARRIER_CODE_UNKNOWN = 14210;

    private LogisticsErrorCodes() {}
}
