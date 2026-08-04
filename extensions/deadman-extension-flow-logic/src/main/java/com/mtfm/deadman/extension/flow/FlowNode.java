package com.mtfm.deadman.extension.flow;

/**
 * 串行流程节点（Filter 风格）。
 * <p>
 * 完成自身逻辑后须调用 {@code chain.proceed(context)} 进入下一节点；不调用则短路。
 *
 * @param <C> 上下文类型
 */
public interface FlowNode<C extends FlowContext> {

    /**
     * 节点唯一标识（用于拦截器与支流挂载）。
     *
     * @return 节点 ID
     */
    String nodeId();

    /**
     * 执行本节点逻辑。
     *
     * @param context 流程上下文
     * @param chain 流程链，业务完成后调用 {@link FlowChain#proceed} 继续
     */
    void execute(C context, FlowChain<?, C> chain);
}
