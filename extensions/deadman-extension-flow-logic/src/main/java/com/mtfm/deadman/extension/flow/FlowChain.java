package com.mtfm.deadman.extension.flow;

/**
 * 执行链：涵盖一整段业务逻辑组合的容器。
 * <p>
 * 可以：
 * <ul>
 *   <li>独立存在并由执行器直接 {@link #execute}（如结算链、履约链）</li>
 *   <li>作为子链嵌入父链或互斥/并行网关分支</li>
 * </ul>
 * 链内部由有序 {@link FlowElement} 组成，推进细节由实现类委托引擎完成。
 *
 * @param <K> 流程类型键（常用枚举；轻量子链可用 String）
 * @param <C> 上下文类型
 */
public interface FlowChain<K, C extends FlowContext> {

    /**
     * 本链对应的流程类型键。
     * <p>
     * 用于 {@link FlowChainRegistry} 注册/查找，以及日志中的链标识。
     *
     * @return 流程类型键，不可为 null
     */
    K flowType();

    /**
     * 从链头开始执行全部元素，直至结束节点、abort 或元素耗尽。
     *
     * @param context 流程上下文（整条链共享）
     */
    void execute(C context);
}
