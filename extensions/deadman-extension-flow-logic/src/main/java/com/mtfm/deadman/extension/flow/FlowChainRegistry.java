package com.mtfm.deadman.extension.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 按流程类型键注册并查找流程链。
 *
 * @param <K> 流程类型键
 * @param <C> 上下文类型
 */
public final class FlowChainRegistry<K, C extends FlowContext> {

    private final Map<K, FlowChain<K, C>> chains;

    private FlowChainRegistry(Map<K, FlowChain<K, C>> chains) {
        this.chains = Map.copyOf(chains);
    }

    /**
     * 由链列表构建注册表（同一 flowType 不可重复）。
     *
     * @param chainList 已编排的链
     * @param <K> 流程类型键
     * @param <C> 上下文类型
     * @return 注册表
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
     * 按流程类型获取链。
     *
     * @param flowType 流程类型键
     * @return 流程链
     */
    public FlowChain<K, C> require(K flowType) {
        FlowChain<K, C> chain = chains.get(flowType);
        if (chain == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "未配置流程链: " + flowType);
        }
        return chain;
    }

    /**
     * 已注册的流程类型数量。
     *
     * @return 数量
     */
    public int size() {
        return chains.size();
    }
}
