package com.mtfm.deadman.plugin.logistics.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class LogisticsMessages {

    private LogisticsMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case LogisticsErrorCodes.LOGISTICS_PROVIDER_NOT_FOUND -> ex(code, "result.LOGISTICS_PROVIDER_NOT_FOUND", "物流 Provider 不存在");
            case LogisticsErrorCodes.LOGISTICS_TRACK_QUERY_FAILED -> ex(code, "result.LOGISTICS_TRACK_QUERY_FAILED", "快递轨迹查询失败");
            case LogisticsErrorCodes.LOGISTICS_CONFIG_INVALID -> ex(code, "result.LOGISTICS_CONFIG_INVALID", "物流插件配置无效");
            case LogisticsErrorCodes.LOGISTICS_CARRIER_DETECT_FAILED -> ex(code, "result.LOGISTICS_CARRIER_DETECT_FAILED", "快递公司识别失败");
            case LogisticsErrorCodes.LOGISTICS_SUBSCRIBE_FAILED -> ex(code, "result.LOGISTICS_SUBSCRIBE_FAILED", "快递轨迹订阅失败");
            case LogisticsErrorCodes.LOGISTICS_SUBSCRIBE_PUSH_INVALID -> ex(code, "result.LOGISTICS_SUBSCRIBE_PUSH_INVALID", "订阅推送验签失败");
            case LogisticsErrorCodes.LOGISTICS_WAYBILL_FAILED -> ex(code, "result.LOGISTICS_WAYBILL_FAILED", "电子面单操作失败");
            case LogisticsErrorCodes.LOGISTICS_SHIP_ORDER_FAILED -> ex(code, "result.LOGISTICS_SHIP_ORDER_FAILED", "寄件下单失败");
            case LogisticsErrorCodes.LOGISTICS_SHIP_CANCEL_FAILED -> ex(code, "result.LOGISTICS_SHIP_CANCEL_FAILED", "寄件取消失败");
            case LogisticsErrorCodes.LOGISTICS_CARRIER_CODE_UNKNOWN -> ex(code, "result.LOGISTICS_CARRIER_CODE_UNKNOWN", "快递公司编码未注册或不支持当前渠道");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
