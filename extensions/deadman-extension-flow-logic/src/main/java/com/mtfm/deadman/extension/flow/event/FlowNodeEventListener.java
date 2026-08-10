package com.mtfm.deadman.extension.flow.event;

import com.mtfm.deadman.extension.flow.FlowContext;

/**
 * 节点成功事件监听器：由引擎在普通节点/结束节点执行成功后回调。
 *
 * @param <C> 上下文类型
 */
@FunctionalInterface
public interface FlowNodeEventListener<C extends FlowContext> {

    /**
     * 节点执行成功。
     *
     * @param elementId 成功节点 ID
     * @param context 流程上下文（与执行链共享同一实例）
     */
    void onNodeSuccess(String elementId, C context);

    /**
     * 适配为事件对象回调（便于需要 record 形态的监听方）。
     *
     * @param event 成功事件
     */
    default void onNodeSuccess(FlowNodeSuccessEvent<? extends C> event) {
        onNodeSuccess(event.elementId(), event.context());
    }
}
