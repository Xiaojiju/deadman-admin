package com.mtfm.deadman.plugin.logistics.kuaidi100.vo;

/**
 * 快递100 轨迹订阅推送应答体。
 * <p>
 * 本接口响应遵循快递100 回调协议，不使用项目统一 {@code Result} 封装。
 *
 * @param result     是否成功
 * @param returnCode 返回码
 * @param message    描述信息
 */
public record Kuaidi100SubscribeNotifyAck(boolean result, String returnCode, String message) {

    /**
     * 构建成功应答。
     *
     * @return 成功应答
     */
    public static Kuaidi100SubscribeNotifyAck success() {
        return new Kuaidi100SubscribeNotifyAck(true, "200", "成功");
    }

    /**
     * 构建失败应答。
     *
     * @return 失败应答
     */
    public static Kuaidi100SubscribeNotifyAck failure() {
        return new Kuaidi100SubscribeNotifyAck(false, "500", "失败");
    }
}
