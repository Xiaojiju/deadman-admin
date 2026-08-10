package com.mtfm.deadman.extension.flow.event;

import com.mtfm.deadman.extension.flow.FlowContext;

/**
 * 流程事件发布器：可供节点注入，在业务成功点主动发布成功事件。
 * <p>
 * 引擎在 {@link com.mtfm.deadman.extension.flow.node.LogicNode}/{@link com.mtfm.deadman.extension.flow.node.EndNode}
 * 执行成功后也会自动通知链上的 {@link FlowNodeEventListener}；本接口用于节点内额外/自定义发布
 * （例如对接 Spring {@code ApplicationEventPublisher}）。
 *
 * @param <C> 上下文类型
 */
@FunctionalInterface
public interface FlowEventPublisher<C extends FlowContext> {

    /**
     * 发布节点成功事件。
     *
     * @param elementId 节点 ID
     * @param context 流程上下文
     */
    void publishSuccess(String elementId, C context);
}
