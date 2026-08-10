package com.mtfm.deadman.extension.flow.node;

import com.mtfm.deadman.extension.flow.FlowContext;
import com.mtfm.deadman.extension.flow.FlowElement;
import com.mtfm.deadman.extension.flow.event.FlowEventPublisher;

/**
 * 普通逻辑节点：流程中的基础计算单元，分步处理业务任务。
 * <p>
 * 引擎在 {@link #run} 成功后会自动通知链级 {@link com.mtfm.deadman.extension.flow.event.FlowNodeEventListener}。
 * 若节点额外注入了 {@link FlowEventPublisher}，可在 {@link #execute} 内主动发布自定义成功/业务事件。
 *
 * @param <C> 上下文类型
 */
public abstract class LogicNode<C extends FlowContext> implements FlowElement<C> {

    /**
     * 可选事件发布器，供子类在业务逻辑中主动发布事件（如对接 Spring ApplicationEvent）。
     */
    private FlowEventPublisher<? super C> eventPublisher;

    /**
     * 节点业务标识，在同一流程编排内应唯一。
     *
     * @return 节点 ID
     */
    public abstract String nodeId();

    @Override
    public final String elementId() {
        return nodeId();
    }

    @Override
    public final boolean notifiableNode() {
        return true;
    }

    /**
     * 注入事件发布器（Spring 可对具体节点 Bean 注入后调用，或字段注入后由子类使用）。
     *
     * @param eventPublisher 发布器，可为 null
     */
    public void setEventPublisher(FlowEventPublisher<? super C> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 供子类在 {@link #execute} 中主动发事件。
     *
     * @return 发布器，可能为 null
     */
    protected FlowEventPublisher<? super C> eventPublisher() {
        return eventPublisher;
    }

    /**
     * 发布本节点成功事件（需已注入 {@link FlowEventPublisher}；未注入则忽略）。
     *
     * @param context 流程上下文
     */
    protected final void publishSuccess(C context) {
        FlowEventPublisher<? super C> publisher = eventPublisher;
        if (publisher != null) {
            publisher.publishSuccess(nodeId(), context);
        }
    }

    /**
     * 执行业务；成功后由引擎通知链级监听器。
     *
     * @param context 流程上下文
     * @return 始终 false
     */
    @Override
    public final boolean run(C context) {
        execute(context);
        return false;
    }

    /**
     * 执行本节点业务逻辑。
     *
     * @param context 流程上下文
     */
    public abstract void execute(C context);
}
