package com.mtfm.deadman.plugin.websocket.dispatch;

/**
 * 消息投递拦截器：在发送成功或达到最终失败状态后回调，默认实现为空操作。
 */
public interface MessageDeliveryInterceptor {

    /**
     * 投递完成（成功或最终失败）后触发。
     *
     * @param context 投递上下文
     */
    default void afterDelivery(MessageDeliveryContext context) {
    }
}
