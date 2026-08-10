package com.mtfm.deadman.extension.flow.node;

import com.mtfm.deadman.extension.flow.FlowContext;
import com.mtfm.deadman.extension.flow.FlowElement;
import com.mtfm.deadman.extension.flow.event.FlowEventPublisher;

/**
 * 结束节点：当前逻辑链执行完毕后的收尾任务。
 * <p>
 * {@link #run} 在 {@link #complete} 成功后返回 true，终止当前链；引擎随后通知链级成功监听器。
 *
 * @param <C> 上下文类型
 */
public abstract class EndNode<C extends FlowContext> implements FlowElement<C> {

    /**
     * 可选事件发布器，供 {@link #complete} 内主动发事件。
     */
    private FlowEventPublisher<? super C> eventPublisher;

    /**
     * 节点业务标识。
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
     * 注入事件发布器。
     *
     * @param eventPublisher 发布器，可为 null
     */
    public void setEventPublisher(FlowEventPublisher<? super C> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * @return 发布器，可能为 null
     */
    protected FlowEventPublisher<? super C> eventPublisher() {
        return eventPublisher;
    }

    /**
     * 发布本节点成功事件（需已注入发布器）。
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
     * 执行收尾并终止当前链。
     *
     * @param context 流程上下文
     * @return 始终 true
     */
    @Override
    public final boolean run(C context) {
        complete(context);
        return true;
    }

    /**
     * 执行结束收尾逻辑。
     *
     * @param context 流程上下文
     */
    public abstract void complete(C context);
}
