package com.mtfm.deadman.extension.flow.gateway;

import java.util.Objects;

import com.mtfm.deadman.extension.flow.FlowChain;
import com.mtfm.deadman.extension.flow.FlowContext;
import com.mtfm.deadman.extension.flow.NestedFlowChain;
import com.mtfm.deadman.extension.flow.node.LogicNode;

/**
 * 互斥网关的一条分支：条件分支或默认分支。
 *
 * @param condition 命中条件；默认分支可为 null
 * @param target 命中后执行的子链
 * @param defaultBranch 是否为默认分支
 * @param <C> 上下文类型
 */
public record ExclusiveBranch<C extends FlowContext>(
    /** 分支命中条件；默认分支时为 null */
    FlowCondition<C> condition,
    /** 分支目标子链 */
    FlowChain<?, C> target,
    /** 是否默认分支 */
    boolean defaultBranch) {

    /**
     * @param condition 条件（默认分支允许 null）
     * @param target 目标子链
     * @param defaultBranch 是否默认分支
     */
    public ExclusiveBranch {
        Objects.requireNonNull(target, "target");
        if (!defaultBranch) {
            Objects.requireNonNull(condition, "condition");
        }
    }

    /**
     * 条件分支（目标为子链）。
     *
     * @param condition 判断条件
     * @param target 子链
     * @param <C> 上下文类型
     * @return 条件分支
     */
    public static <C extends FlowContext> ExclusiveBranch<C> of(FlowCondition<C> condition, FlowChain<?, C> target) {
        return new ExclusiveBranch<>(condition, target, false);
    }

    /**
     * 条件分支（目标为单个普通节点，内部自动包装为轻量子链）。
     *
     * @param condition 判断条件
     * @param node 普通节点
     * @param <C> 上下文类型
     * @return 条件分支
     */
    public static <C extends FlowContext> ExclusiveBranch<C> of(FlowCondition<C> condition, LogicNode<C> node) {
        Objects.requireNonNull(node, "node");
        return of(condition, NestedFlowChain.of(node.nodeId() + "-branch", node));
    }

    /**
     * 默认分支（目标为子链）。
     *
     * @param target 默认子链
     * @param <C> 上下文类型
     * @return 默认分支
     */
    public static <C extends FlowContext> ExclusiveBranch<C> defaultOf(FlowChain<?, C> target) {
        return new ExclusiveBranch<>(null, target, true);
    }

    /**
     * 默认分支（目标为单个普通节点，内部自动包装为轻量子链）。
     *
     * @param node 普通节点
     * @param <C> 上下文类型
     * @return 默认分支
     */
    public static <C extends FlowContext> ExclusiveBranch<C> defaultOf(LogicNode<C> node) {
        Objects.requireNonNull(node, "node");
        return defaultOf(NestedFlowChain.of(node.nodeId() + "-branch", node));
    }
}
