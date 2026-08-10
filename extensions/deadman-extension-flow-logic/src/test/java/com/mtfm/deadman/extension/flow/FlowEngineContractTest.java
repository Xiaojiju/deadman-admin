package com.mtfm.deadman.extension.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

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
 * 流程引擎契约：错误处理、事件顺序、并行 fork 约束、元素定位。
 */
class FlowEngineContractTest {

    @Test
    void errorHandlerMustThrow_otherwiseEngineFails() {
        TestContext context = new TestContext();
        LogicNode<TestContext> boom = node("boom", ctx -> {
            throw new IllegalStateException("bad");
        });
        FlowErrorHandler<TestContext> silent = event -> {
            // 故意不抛 —— 违反契约
        };
        assertThatThrownBy(
                () -> new NestedFlowChain<>("c1", List.of(boom), silent, List.of()).execute(context))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining("FlowErrorHandler 必须抛出异常");
    }

    @Test
    void errorHandlerCanConvertToBusinessException() {
        TestContext context = new TestContext();
        LogicNode<TestContext> boom = node("login-form", ctx -> {
            throw new IllegalArgumentException("illegal char");
        });
        FlowErrorHandler<TestContext> handler = event -> {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名格式错误", event.error());
        };
        assertThatThrownBy(() -> run(List.of(boom), handler, List.of(), context))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名格式错误");
    }

    @Test
    void nestedFailureReportsInnerElementId() {
        TestContext context = new TestContext();
        LogicNode<TestContext> inner = node("inner-node", ctx -> {
            throw new IllegalStateException("x");
        });
        NestedFlowChain<TestContext> nested = NestedFlowChain.of("sub", inner);
        ExclusiveGateway<TestContext> gateway = new ExclusiveGateway<>("gw",
                List.of(ExclusiveBranch.defaultOf(nested)));
        FlowErrorHandler<TestContext> handler = event -> {
            throw new BusinessException(ResultCode.BAD_REQUEST, "fail@" + event.elementId(), event.error());
        };
        assertThatThrownBy(() -> run(List.of(gateway), handler, List.of(), context))
                .isInstanceOf(BusinessException.class)
                .hasMessage("fail@inner-node");
    }

    @Test
    void successEventFiresAfterAfterInterceptor() {
        TestContext context = new TestContext();
        List<String> order = new ArrayList<>();
        LogicNode<TestContext> ok = node("n1", ctx -> order.add("run"));
        FlowInterceptor<TestContext> interceptor = new FlowInterceptor<>() {
            @Override
            public void beforeElement(String elementId, TestContext ctx) {
                order.add("before");
            }

            @Override
            public void afterElement(String elementId, TestContext ctx) {
                order.add("after");
            }
        };
        FlowNodeEventListener<TestContext> listener = (id, ctx) -> order.add("event");
        run(List.of(ok), null, List.of(listener), context, List.of(interceptor));
        assertThat(order).containsExactly("before", "run", "after", "event");
    }

    @Test
    void interceptorFailureGoesThroughErrorHandler() {
        TestContext context = new TestContext();
        LogicNode<TestContext> ok = node("n1", ctx -> {
        });
        FlowInterceptor<TestContext> interceptor = new FlowInterceptor<>() {
            @Override
            public void afterElement(String elementId, TestContext ctx) {
                throw new IllegalStateException("after-fail");
            }
        };
        FlowErrorHandler<TestContext> handler = event -> {
            throw new BusinessException(ResultCode.BAD_REQUEST, "wrapped-" + event.elementId(), event.error());
        };
        assertThatThrownBy(() -> run(List.of(ok), handler, List.of(), context, List.of(interceptor)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("wrapped-n1");
    }

    @Test
    void parallelRequiresFork() {
        TestContext context = new TestContext();
        LogicNode<TestContext> a = node("a", ctx -> {
        });
        LogicNode<TestContext> b = node("b", ctx -> {
        });
        ParallelGateway<TestContext> parallel = new ParallelGateway<>("p",
                List.of(NestedFlowChain.of("ba", a), NestedFlowChain.of("bb", b)));
        assertThatThrownBy(() -> run(List.of(parallel), null, List.of(), context))
                .isInstanceOf(FlowElementException.class)
                .cause()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("forkForParallelBranch");
    }

    @Test
    void parallelWithForkIsolatesBranches() {
        AtomicInteger writes = new AtomicInteger();
        ForkableContext context = new ForkableContext();
        LogicNode<ForkableContext> a = nodeF("pa", ctx -> {
            ctx.token = "A";
            writes.incrementAndGet();
        });
        LogicNode<ForkableContext> b = nodeF("pb", ctx -> {
            ctx.token = "B";
            writes.incrementAndGet();
        });
        ParallelGateway<ForkableContext> parallel = new ParallelGateway<>("p",
                List.of(NestedFlowChain.of("ba", a), NestedFlowChain.of("bb", b)));
        run(List.of(parallel), null, List.of(), context);
        assertThat(writes.get()).isEqualTo(2);
        assertThat(context.token).isNull();
    }

    @Test
    void endNodeStopsCurrentChain() {
        TestContext context = new TestContext();
        AtomicInteger after = new AtomicInteger();
        EndNode<TestContext> end = new EndNode<>() {
            @Override
            public String nodeId() {
                return "end";
            }

            @Override
            public void complete(TestContext ctx) {
                ctx.putAttr("done", Boolean.TRUE);
            }
        };
        LogicNode<TestContext> shouldNotRun = node("after-end", ctx -> after.incrementAndGet());
        run(List.of(end, shouldNotRun), null, List.of(), context);
        assertThat(context.getAttr("done", Boolean.class)).isTrue();
        assertThat(after.get()).isZero();
    }

    private static LogicNode<TestContext> node(String id, Consumer<TestContext> body) {
        return new LogicNode<>() {
            @Override
            public String nodeId() {
                return id;
            }

            @Override
            public void execute(TestContext context) {
                body.accept(context);
            }
        };
    }

    private static LogicNode<ForkableContext> nodeF(String id, Consumer<ForkableContext> body) {
        return new LogicNode<>() {
            @Override
            public String nodeId() {
                return id;
            }

            @Override
            public void execute(ForkableContext context) {
                body.accept(context);
            }
        };
    }

    private static <C extends FlowContext> void run(List<? extends FlowElement<C>> elements,
            FlowErrorHandler<? super C> errorHandler, List<? extends FlowNodeEventListener<? super C>> listeners,
            C context) {
        run(elements, errorHandler, listeners, context, List.of());
    }

    private static <C extends FlowContext> void run(List<? extends FlowElement<C>> elements,
            FlowErrorHandler<? super C> errorHandler, List<? extends FlowNodeEventListener<? super C>> listeners,
            C context, List<? extends FlowInterceptor<? super C>> interceptors) {
        FlowEngine.execute("test", elements, interceptors, errorHandler, listeners, context);
    }

    static class TestContext extends FlowContext {
    }

    static class ForkableContext extends FlowContext {
        String token;

        @Override
        public FlowContext forkForParallelBranch() {
            ForkableContext branch = new ForkableContext();
            copyBaseStateTo(branch);
            branch.token = this.token;
            return branch;
        }
    }
}
