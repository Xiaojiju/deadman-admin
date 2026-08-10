package com.mtfm.deadman.extension.flow;

/**
 * 流程可编排单元根接口。
 * <p>
 * 凡能挂到执行链上的对象均实现本接口，包括普通节点、结束节点、网关与子链。
 * 元素通过 {@link #run(FlowContext)} 自描述执行语义，引擎不再依赖 instanceof 硬编码分发。
 *
 * @param <C> 上下文类型
 */
public interface FlowElement<C extends FlowContext> {

    /**
     * 元素唯一标识（拦截器、错误处理、事件与日志定位）。
     *
     * @return 元素 ID，不可为 null
     */
    String elementId();

    /**
     * 执行本元素。
     *
     * @param context 流程上下文
     * @return {@code true} 表示应终止<strong>当前链</strong>后续元素（如结束节点）；
     *         {@code false} 表示继续推进
     */
    boolean run(C context);

    /**
     * 是否为可发布成功事件的业务节点（普通节点 / 结束节点）。
     * <p>
     * 网关与子链容器默认 false，避免把容器执行误报为节点成功。
     *
     * @return 是业务节点则为 true
     */
    default boolean notifiableNode() {
        return false;
    }
}
