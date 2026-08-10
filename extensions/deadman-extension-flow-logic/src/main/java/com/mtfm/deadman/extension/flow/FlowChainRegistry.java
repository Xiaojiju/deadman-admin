package com.mtfm.deadman.extension.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 流程链注册表：按流程类型键注册并查找可执行链。
 * <p>
 * 典型用法：在领域执行器构造时 {@link #of(List)} 收集多条链，运行时 {@link #require(Object)} 按场景取链并
 * {@link FlowChain#execute}。
 *
 * @param <K> 流程类型键（常用枚举）
 * @param <C> 上下文类型
 */
public final class FlowChainRegistry<K, C extends FlowContext> {

    /**
     * 流程类型 → 执行链 的不可变映射。
     * <p>
     * 构造时通过 {@link Map#copyOf} 冻结，保证注册表线程安全只读。
     */
    private final Map<K, FlowChain<K, C>> chains;

    /**
     * @param chains 已去重并拷贝的链映射
     */
    private FlowChainRegistry(Map<K, FlowChain<K, C>> chains) {
        this.chains = Map.copyOf(chains);
    }

    /**
     * 由链列表构建注册表。
     * <p>
     * 同一 {@code flowType} 出现两次将抛出 {@link IllegalStateException}。
     *
     * @param chainList 已编排的链列表
     * @param <K> 流程类型键
     * @param <C> 上下文类型
     * @return 不可变注册表实例
     */
    public static <K, C extends FlowContext> FlowChainRegistry<K, C> of(
        List<? extends FlowChain<K, C>> chainList) {
        Objects.requireNonNull(chainList, "chainList");
        Map<K, FlowChain<K, C>> map = new HashMap<>();
        for (FlowChain<K, C> chain : chainList) {
            K type = chain.flowType();
            if (map.containsKey(type)) {
                throw new IllegalStateException("重复注册流程链: " + type);
            }
            map.put(type, chain);
        }
        return new FlowChainRegistry<>(map);
    }

    /**
     * 按流程类型获取链；未注册时抛出业务异常。
     *
     * @param flowType 流程类型键
     * @return 对应执行链，永不返回 null
     */
    public FlowChain<K, C> require(K flowType) {
        FlowChain<K, C> chain = chains.get(flowType);
        if (chain == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "未配置流程链: " + flowType);
        }
        return chain;
    }

    /**
     * 已注册的流程类型数量（便于测试与诊断）。
     *
     * @return 注册数量
     */
    public int size() {
        return chains.size();
    }
}
