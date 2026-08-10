package com.mtfm.deadman.extension.flow.event;

import java.util.List;
import java.util.Objects;

import com.mtfm.deadman.extension.flow.FlowContext;

/**
 * 组合事件发布器：将成功事件转发给多个 {@link FlowNodeEventListener}。
 * <p>
 * 可注册为 Spring Bean 供节点注入，与链级监听器共用同一批监听实现。
 *
 * @param <C> 上下文类型
 */
public final class CompositeFlowEventPublisher<C extends FlowContext> implements FlowEventPublisher<C> {

    /** 监听器列表（不可变） */
    private final List<? extends FlowNodeEventListener<? super C>> listeners;

    /**
     * @param listeners 监听器；可为 null/empty
     */
    public CompositeFlowEventPublisher(List<? extends FlowNodeEventListener<? super C>> listeners) {
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
    }

    /**
     * 依次通知全部监听器。
     *
     * @param elementId 节点 ID
     * @param context 流程上下文
     */
    @Override
    public void publishSuccess(String elementId, C context) {
        Objects.requireNonNull(elementId, "elementId");
        Objects.requireNonNull(context, "context");
        for (FlowNodeEventListener<? super C> listener : listeners) {
            listener.onNodeSuccess(elementId, context);
        }
    }
}
