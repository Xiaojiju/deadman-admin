package com.mtfm.deadman.extension.flow;

/**
 * 串行流程链（语义对齐 Servlet {@code FilterChain}）。
 * <p>
 * 节点完成自身逻辑后须调用 {@link #proceed} 才会进入下一节点；不调用即短路。
 *
 * @param <K> 流程类型键（常用枚举）
 * @param <C> 上下文类型
 */
public interface FlowChain<K, C extends FlowContext> {

    /**
     * 本链对应的流程类型。
     *
     * @return 流程类型键
     */
    K flowType();

    /**
     * 推进到下一节点（入口由执行器调用；节点执行完后自行再调）。
     *
     * @param context 流程上下文
     */
    void proceed(C context);
}
