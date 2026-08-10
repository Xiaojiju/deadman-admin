package com.mtfm.deadman.extension.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.extension.flow.error.FlowErrorHandler;
import com.mtfm.deadman.extension.flow.event.FlowNodeEventListener;
import com.mtfm.deadman.extension.flow.gateway.ExclusiveBranch;
import com.mtfm.deadman.extension.flow.gateway.ExclusiveGateway;
import com.mtfm.deadman.extension.flow.gateway.ParallelGateway;
import com.mtfm.deadman.extension.flow.node.EndNode;
import com.mtfm.deadman.extension.flow.node.LogicNode;

/**
 * 流程链抽象基类：负责元素编排与引擎推进。
 * <p>
 * 支持普通节点、互斥/并行网关、结束节点、嵌套子链；可挂拦截器、错误处理器与节点成功事件监听。
 *
 * @param <K> 流程类型键
 * @param <C> 上下文类型
 */
public abstract class AbstractFlowChain<K, C extends FlowContext> implements FlowChain<K, C>, FlowElement<C> {

    /** 流程类型键 */
    private final K flowType;

    /** Spring Bean 工厂 */
    private final BeanFactory beanFactory;

    /** 编排期临时列表 */
    private final List<FlowElement<C>> buildingElements = new ArrayList<>();

    /** 冻结后的有序元素 */
    private final List<FlowElement<C>> elements;

    /** 横切拦截器 */
    private final List<? extends FlowInterceptor<? super C>> interceptors;

    /** 错误处理器（可为 null） */
    private final FlowErrorHandler<? super C> errorHandler;

    /** 节点成功事件监听器 */
    private final List<? extends FlowNodeEventListener<? super C>> eventListeners;

    /** 是否已冻结 */
    private boolean configured;

    /**
     * 完整构造并冻结编排。
     *
     * @param flowType 流程类型键
     * @param beanFactory Bean 工厂
     * @param interceptors 拦截器，可为 null
     * @param errorHandler 错误处理器，可为 null（表示异常原样抛出）
     * @param eventListeners 节点成功监听器，可为 null
     */
    protected AbstractFlowChain(K flowType, BeanFactory beanFactory,
            List<? extends FlowInterceptor<? super C>> interceptors, FlowErrorHandler<? super C> errorHandler,
            List<? extends FlowNodeEventListener<? super C>> eventListeners) {
        this.flowType = Objects.requireNonNull(flowType, "flowType");
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory");
        this.interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
        this.errorHandler = errorHandler;
        this.eventListeners = eventListeners == null ? List.of() : List.copyOf(eventListeners);
        configure();
        this.configured = true;
        if (buildingElements.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "流程元素未配置: " + flowType);
        }
        validateEndNodePosition();
        validateUniqueElementIds();
        this.elements = List.copyOf(buildingElements);
        buildingElements.clear();
    }

    /**
     * 仅 BeanFactory 的便捷构造。
     *
     * @param flowType 流程类型键
     * @param beanFactory Bean 工厂
     */
    protected AbstractFlowChain(K flowType, BeanFactory beanFactory) {
        this(flowType, beanFactory, List.of(), null, List.of());
    }

    /**
     * 带拦截器的便捷构造。
     *
     * @param flowType 流程类型键
     * @param beanFactory Bean 工厂
     * @param interceptors 拦截器
     */
    protected AbstractFlowChain(K flowType, BeanFactory beanFactory,
            List<? extends FlowInterceptor<? super C>> interceptors) {
        this(flowType, beanFactory, interceptors, null, List.of());
    }

    /**
     * 子类在此编排元素。
     */
    protected abstract void configure();

    /**
     * 追加单个普通逻辑节点。
     *
     * @param nodeType 节点类型
     * @return this
     */
    @SuppressWarnings("unchecked")
    protected final AbstractFlowChain<K, C> addLogic(Class<? extends LogicNode<?>> nodeType) {
        Objects.requireNonNull(nodeType, "nodeType");
        return addElement((FlowElement<C>) beanFactory.getBean(nodeType));
    }

    /**
     * 批量追加普通逻辑节点。
     *
     * @param nodeTypes 节点类型序列
     * @return this
     */
    @SafeVarargs
    protected final AbstractFlowChain<K, C> addLogic(Class<? extends LogicNode<?>>... nodeTypes) {
        Objects.requireNonNull(nodeTypes, "nodeTypes");
        for (Class<? extends LogicNode<?>> nodeType : nodeTypes) {
            addLogic(nodeType);
        }
        return this;
    }

    /**
     * 追加结束节点（必须位于链尾，构造期校验）。
     *
     * @param endNodeType 结束节点类型
     * @return this
     */
    @SuppressWarnings("unchecked")
    protected final AbstractFlowChain<K, C> addEnd(Class<? extends EndNode<?>> endNodeType) {
        Objects.requireNonNull(endNodeType, "endNodeType");
        return addElement((FlowElement<C>) beanFactory.getBean(endNodeType));
    }

    /**
     * 追加互斥网关。
     *
     * @param gatewayId 网关 ID
     * @param branches 分支（含默认）
     * @return this
     */
    @SafeVarargs
    protected final AbstractFlowChain<K, C> addExclusive(String gatewayId, ExclusiveBranch<C>... branches) {
        Objects.requireNonNull(branches, "branches");
        return addElement(new ExclusiveGateway<>(gatewayId, List.of(branches)));
    }

    /**
     * 追加并行网关（子链）。
     *
     * @param gatewayId 网关 ID
     * @param branches 并行子链
     * @return this
     */
    @SafeVarargs
    protected final AbstractFlowChain<K, C> addParallel(String gatewayId, FlowChain<?, C>... branches) {
        Objects.requireNonNull(branches, "branches");
        return addElement(new ParallelGateway<>(gatewayId, List.of(branches)));
    }

    /**
     * 追加并行网关（单个普通节点自动包装为子链）。
     *
     * @param gatewayId 网关 ID
     * @param nodes 并行普通节点
     * @return this
     */
    @SafeVarargs
    protected final AbstractFlowChain<K, C> addParallelNodes(String gatewayId, LogicNode<C>... nodes) {
        Objects.requireNonNull(nodes, "nodes");
        List<FlowChain<?, C>> branches = new ArrayList<>(nodes.length);
        for (LogicNode<C> node : nodes) {
            Objects.requireNonNull(node, "node");
            branches.add(NestedFlowChain.of(node.nodeId() + "-branch", node));
        }
        return addElement(new ParallelGateway<>(gatewayId, branches));
    }

    /**
     * 按类型解析节点后追加并行网关。
     *
     * @param gatewayId 网关 ID
     * @param nodeTypes 节点类型
     * @return this
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    protected final AbstractFlowChain<K, C> addParallel(String gatewayId, Class<? extends LogicNode<?>>... nodeTypes) {
        Objects.requireNonNull(nodeTypes, "nodeTypes");
        List<FlowChain<?, C>> branches = new ArrayList<>(nodeTypes.length);
        for (Class<? extends LogicNode<?>> nodeType : nodeTypes) {
            LogicNode<C> node = (LogicNode<C>) beanFactory.getBean(nodeType);
            branches.add(NestedFlowChain.of(node.nodeId() + "-branch", node));
        }
        return addElement(new ParallelGateway<>(gatewayId, branches));
    }

    /**
     * 嵌入子链。
     *
     * @param subChain 子链
     * @return this
     */
    protected final AbstractFlowChain<K, C> addSubChain(FlowChain<?, C> subChain) {
        Objects.requireNonNull(subChain, "subChain");
        if (subChain instanceof FlowElement<?> element) {
            @SuppressWarnings("unchecked")
            FlowElement<C> typed = (FlowElement<C>) element;
            return addElement(typed);
        }
        return addElement(new FlowChainElementAdapter<>(subChain));
    }

    /**
     * 直接追加元素。
     *
     * @param element 元素
     * @return this
     */
    protected final AbstractFlowChain<K, C> addElement(FlowElement<C> element) {
        ensureConfiguring();
        buildingElements.add(Objects.requireNonNull(element, "element"));
        return this;
    }

    /**
     * 构建轻量子链。
     *
     * @param subChainId 子链 ID
     * @param elements 元素
     * @return 子链
     */
    @SafeVarargs
    protected final NestedFlowChain<C> subChain(String subChainId, FlowElement<C>... elements) {
        return NestedFlowChain.of(subChainId, elements);
    }

    /**
     * 按类型构建仅含普通节点的轻量子链。
     *
     * @param subChainId 子链 ID
     * @param nodeTypes 节点类型
     * @return 子链
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    protected final NestedFlowChain<C> subChain(String subChainId, Class<? extends LogicNode<?>>... nodeTypes) {
        Objects.requireNonNull(nodeTypes, "nodeTypes");
        List<FlowElement<C>> list = new ArrayList<>(nodeTypes.length);
        for (Class<? extends LogicNode<?>> nodeType : nodeTypes) {
            list.add((FlowElement<C>) beanFactory.getBean(nodeType));
        }
        return new NestedFlowChain<>(subChainId, list);
    }

    @Override
    public final String elementId() {
        return String.valueOf(flowType);
    }

    @Override
    public final K flowType() {
        return flowType;
    }

    @Override
    public final boolean run(C context) {
        execute(context);
        return false;
    }

    @Override
    public final void execute(C context) {
        FlowEngine.execute(String.valueOf(flowType), elements, interceptors, errorHandler, eventListeners, context);
    }

    /**
     * 结束节点若存在，必须位于元素列表最后一位。
     */
    private void validateEndNodePosition() {
        for (int i = 0; i < buildingElements.size(); i++) {
            FlowElement<C> element = buildingElements.get(i);
            if (element instanceof EndNode && i != buildingElements.size() - 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "结束节点必须位于链尾: flowType=" + flowType + ", elementId=" + element.elementId());
            }
        }
    }

    /**
     * 同一条链顶层元素 ID 必须唯一（含网关/子链容器 ID）。
     */
    private void validateUniqueElementIds() {
        Set<String> ids = new HashSet<>();
        for (FlowElement<C> element : buildingElements) {
            String id = element.elementId();
            if (id == null || id.isBlank()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "流程元素 ID 不能为空: flowType=" + flowType);
            }
            if (!ids.add(id)) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "流程元素 ID 重复: flowType=" + flowType + ", elementId=" + id);
            }
        }
    }

    private void ensureConfiguring() {
        if (configured) {
            throw new IllegalStateException("流程链已冻结，不可再编排元素: " + flowType);
        }
    }

    /**
     * 将未实现 {@link FlowElement} 的链适配为可编排元素。
     *
     * @param <C> 上下文类型
     */
    private static final class FlowChainElementAdapter<C extends FlowContext>
            implements FlowElement<C>, FlowChain<Object, C> {

        /** 被适配的链 */
        private final FlowChain<?, C> chain;

        private FlowChainElementAdapter(FlowChain<?, C> chain) {
            this.chain = chain;
        }

        @Override
        public String elementId() {
            return String.valueOf(chain.flowType());
        }

        @Override
        public Object flowType() {
            return chain.flowType();
        }

        @Override
        public boolean run(C context) {
            chain.execute(context);
            return false;
        }

        @Override
        public void execute(C context) {
            chain.execute(context);
        }
    }
}
