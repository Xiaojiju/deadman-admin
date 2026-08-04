package com.mtfm.deadman.extension.flow;

/**
 * 流程拦截器：在每个主节点 before/after 插入横切逻辑（风控、审计、限流等）。
 *
 * @param <C> 上下文类型
 */
public interface FlowInterceptor<C extends FlowContext> {

    /**
     * 节点执行前。
     *
     * @param nodeId 即将执行的节点 ID
     * @param context 流程上下文
     */
    default void beforeNode(String nodeId, C context) {
    }

    /**
     * 节点执行后（含支流执行前）。
     *
     * @param nodeId 刚执行完的节点 ID
     * @param context 流程上下文
     */
    default void afterNode(String nodeId, C context) {
    }
}
