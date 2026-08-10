package com.mtfm.deadman.extension.flow;

import java.util.List;
import java.util.Objects;

import com.mtfm.deadman.extension.flow.error.FlowErrorHandler;
import com.mtfm.deadman.extension.flow.event.FlowNodeEventListener;

/**
 * 轻量子链：无需继承 {@link AbstractFlowChain} 即可组装一段可执行序列。
 * <p>
 * 默认不挂拦截器/错误处理器/事件监听（异常向上抛给父链引擎统一兜底）；
 * 可通过带参构造启用。
 *
 * @param <C> 上下文类型
 */
public final class NestedFlowChain<C extends FlowContext> implements FlowChain<String, C>, FlowElement<C> {

    /** 子链 ID（同时作为 flowType） */
    private final String elementId;

    /** 有序元素 */
    private final List<FlowElement<C>> elements;

    /** 错误处理器（可为 null） */
    private final FlowErrorHandler<? super C> errorHandler;

    /** 节点成功监听器 */
    private final List<? extends FlowNodeEventListener<? super C>> eventListeners;

    /**
     * 仅元素序列的轻量子链（无错误处理/事件）。
     *
     * @param elementId 子链 ID
     * @param elements 有序元素
     */
    public NestedFlowChain(String elementId, List<? extends FlowElement<C>> elements) {
        this(elementId, elements, null, List.of());
    }

    /**
     * 完整构造。
     *
     * @param elementId 子链 ID
     * @param elements 有序元素
     * @param errorHandler 错误处理器，可为 null
     * @param eventListeners 事件监听器，可为 null
     */
    public NestedFlowChain(String elementId, List<? extends FlowElement<C>> elements,
            FlowErrorHandler<? super C> errorHandler,
            List<? extends FlowNodeEventListener<? super C>> eventListeners) {
        this.elementId = Objects.requireNonNull(elementId, "elementId");
        Objects.requireNonNull(elements, "elements");
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("子链元素不能为空: " + elementId);
        }
        this.elements = List.copyOf(elements);
        this.errorHandler = errorHandler;
        this.eventListeners = eventListeners == null ? List.of() : List.copyOf(eventListeners);
    }

    /**
     * 便捷工厂。
     *
     * @param elementId 子链 ID
     * @param elements 有序元素
     * @param <C> 上下文类型
     * @return 子链
     */
    @SafeVarargs
    public static <C extends FlowContext> NestedFlowChain<C> of(String elementId, FlowElement<C>... elements) {
        return new NestedFlowChain<>(elementId, List.of(elements));
    }

    @Override
    public String elementId() {
        return elementId;
    }

    @Override
    public String flowType() {
        return elementId;
    }

    @Override
    public boolean run(C context) {
        execute(context);
        return false;
    }

    @Override
    public void execute(C context) {
        FlowEngine.execute(elementId, elements, List.of(), errorHandler, eventListeners, context);
    }
}
