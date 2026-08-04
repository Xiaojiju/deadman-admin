package com.mtfm.deadman.extension.flow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 流程链抽象基类：负责节点编排与 {@link #proceed} 推进；子类只声明节点顺序。
 * <p>
 * 编排仅支持按插入顺序执行。可通过 {@link #addNode(Class)} 按类型从容器解析，
 * 或 {@link #addNode(FlowNode)} 直接挂实例。
 *
 * @param <K> 流程类型键
 * @param <C> 上下文类型
 */
@Slf4j
public abstract class AbstractFlowChain<K, C extends FlowContext> implements FlowChain<K, C> {

    private final K flowType;
    private final BeanFactory beanFactory;
    private final List<FlowNode<C>> buildingNodes = new ArrayList<>();
    private final List<FlowNode<C>> nodes;
    private final List<? extends FlowInterceptor<? super C>> interceptors;
    private final Map<String, List<FlowBranch<? super C>>> branchesByNodeId;
    private boolean configured;

    /**
     * 构造并完成编排冻结。
     *
     * @param flowType     流程类型键
     * @param beanFactory  Spring Bean 工厂（按类型解析节点）
     * @param interceptors 拦截器（可为空列表）
     * @param branches     支流（可为空列表）
     */
    protected AbstractFlowChain(K flowType, BeanFactory beanFactory,
            List<? extends FlowInterceptor<? super C>> interceptors, List<? extends FlowBranch<? super C>> branches) {
        this.flowType = Objects.requireNonNull(flowType, "flowType");
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory");
        this.interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
        List<? extends FlowBranch<? super C>> allBranches = branches == null ? List.of() : branches;
        this.branchesByNodeId = allBranches.stream().sorted(Comparator.comparingInt(FlowBranch::order))
                .collect(Collectors.groupingBy(FlowBranch::afterNodeId, Collectors.toList()));
        configure();
        this.configured = true;
        if (buildingNodes.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "流程节点未配置: " + flowType);
        }
        this.nodes = List.copyOf(buildingNodes);
        buildingNodes.clear();
    }

    /**
     * 子类在此按执行顺序调用 {@link #addNode} / {@link #addNodes} 完成编排。
     */
    protected abstract void configure();

    /**
     * 按节点类型从容器解析并追加到链尾（执行顺序 = 插入顺序）。
     *
     * @param nodeType 节点 Bean 类型
     * @return this，便于链式调用
     */
    @SuppressWarnings("unchecked")
    protected final AbstractFlowChain<K, C> addNode(Class<? extends FlowNode<?>> nodeType) {
        Objects.requireNonNull(nodeType, "nodeType");
        return addNode((FlowNode<C>) beanFactory.getBean(nodeType));
    }

    /**
     * 直接追加节点实例到链尾（执行顺序 = 插入顺序）。
     *
     * @param node 节点实例
     * @return this，便于链式调用
     */
    protected final AbstractFlowChain<K, C> addNode(FlowNode<C> node) {
        ensureConfiguring();
        buildingNodes.add(Objects.requireNonNull(node, "node"));
        return this;
    }

    /**
     * 按类型批量追加节点（参数顺序即执行顺序）。
     *
     * @param nodeTypes 节点 Bean 类型序列
     * @return this，便于链式调用
     */
    @SafeVarargs
    protected final AbstractFlowChain<K, C> addNodes(Class<? extends FlowNode<?>>... nodeTypes) {
        Objects.requireNonNull(nodeTypes, "nodeTypes");
        for (Class<? extends FlowNode<?>> nodeType : nodeTypes) {
            addNode(nodeType);
        }
        return this;
    }

    @Override
    public final K flowType() {
        return flowType;
    }

    @Override
    public final void proceed(C context) {
        new Cursor(0).proceed(context);
    }

    private void ensureConfiguring() {
        if (configured) {
            throw new IllegalStateException("流程链已冻结，不可再编排节点: " + flowType);
        }
    }

    /**
     * 单次调用游标（类比 Spring Security {@code VirtualFilterChain}）。
     */
    private final class Cursor implements FlowChain<K, C> {

        private int index;

        private Cursor(int index) {
            this.index = index;
        }

        @Override
        public K flowType() {
            return AbstractFlowChain.this.flowType;
        }

        @Override
        public void proceed(C context) {
            if (context.isAborted()) {
                log.info("流程已中断，停止推进: flowType={}, reason={}", flowType, context.getAbortReason());
                return;
            }
            if (index >= nodes.size()) {
                return;
            }
            FlowNode<C> node = nodes.get(index++);
            String nodeId = node.nodeId();
            log.debug("执行流程节点: flowType={}, nodeId={}, index={}", flowType, nodeId, index);
            for (FlowInterceptor<? super C> interceptor : interceptors) {
                interceptor.beforeNode(nodeId, context);
            }
            if (context.isAborted()) {
                return;
            }
            FlowChain<K, C> continuation = new FlowChain<>() {
                @Override
                public K flowType() {
                    return AbstractFlowChain.this.flowType;
                }

                @Override
                public void proceed(C ctx) {
                    for (FlowInterceptor<? super C> interceptor : interceptors) {
                        interceptor.afterNode(nodeId, ctx);
                    }
                    runBranches(nodeId, ctx);
                    Cursor.this.proceed(ctx);
                }
            };
            node.execute(context, continuation);
        }
    }

    private void runBranches(String nodeId, C context) {
        List<FlowBranch<? super C>> nodeBranches = branchesByNodeId.getOrDefault(nodeId, List.of());
        for (FlowBranch<? super C> branch : nodeBranches) {
            if (context.isAborted()) {
                return;
            }
            log.debug("执行流程支流: flowType={}, afterNodeId={}, branch={}", flowType, nodeId,
                    branch.getClass().getSimpleName());
            branch.execute(context);
        }
    }
}
