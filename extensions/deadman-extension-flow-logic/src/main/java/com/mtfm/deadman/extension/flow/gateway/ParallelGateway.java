package com.mtfm.deadman.extension.flow.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.extension.flow.FlowChain;
import com.mtfm.deadman.extension.flow.FlowContext;
import com.mtfm.deadman.extension.flow.FlowElement;
import com.mtfm.deadman.extension.flow.FlowElementException;

import lombok.extern.slf4j.Slf4j;

/**
 * 并行网关：各分支在<strong>隔离的 fork 上下文</strong>上并行执行，全部成功后再继续。
 * <p>
 * 约束：
 * <ul>
 *   <li>Context 必须覆盖 {@link FlowContext#forkForParallelBranch()}，且不得返回父实例</li>
 *   <li>分支写库仍可能在失败后无法回滚——并行仅用于无共享副作用或可幂等的分支</li>
 *   <li>使用进程级共享虚拟线程执行器</li>
 * </ul>
 *
 * @param <C> 上下文类型
 */
@Slf4j
public final class ParallelGateway<C extends FlowContext> implements FlowElement<C> {

    /** 进程级共享虚拟线程执行器 */
    private static final ExecutorService SHARED_VIRTUAL_EXECUTOR =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("flow-parallel-", 0).factory());

    /** 网关元素 ID */
    private final String elementId;

    /** 并行子链 */
    private final List<FlowChain<?, C>> branches;

    /**
     * @param elementId 网关 ID
     * @param branches 并行子链（至少一条）
     */
    public ParallelGateway(String elementId, List<? extends FlowChain<?, C>> branches) {
        this.elementId = Objects.requireNonNull(elementId, "elementId");
        Objects.requireNonNull(branches, "branches");
        if (branches.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "并行网关至少需要一条子链: " + elementId);
        }
        this.branches = List.copyOf(branches);
    }

    @Override
    public String elementId() {
        return elementId;
    }

    @Override
    public boolean run(C context) {
        route(context);
        return false;
    }

    /**
     * 并行执行全部子链并汇合。
     *
     * @param context 父上下文（仅用于 fork，分支不直接写回）
     */
    public void route(C context) {
        log.debug("并行网关启动: gatewayId={}, branchCount={}", elementId, branches.size());
        List<C> branchContexts = new ArrayList<>(branches.size());
        List<Future<?>> futures = new ArrayList<>(branches.size());
        for (FlowChain<?, C> branch : branches) {
            C branchContext = requireForkedContext(context);
            branchContexts.add(branchContext);
            futures.add(SHARED_VIRTUAL_EXECUTOR.submit(() -> {
                if (branchContext.isAborted() || context.isAborted()) {
                    return;
                }
                branch.execute(branchContext);
            }));
        }
        Throwable firstFailure = null;
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                cancelAll(futures);
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "并行网关执行被中断: " + elementId, ex);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                if (firstFailure == null) {
                    firstFailure = cause;
                }
                context.abort("并行分支失败: " + elementId);
                cancelAll(futures);
            }
        }
        if (firstFailure != null) {
            if (firstFailure instanceof FlowElementException fee) {
                throw fee;
            }
            throw new FlowElementException(elementId, firstFailure);
        }
        // 任一分支 abort：传播到父上下文
        for (C branchContext : branchContexts) {
            if (branchContext.isAborted()) {
                context.abort(branchContext.getAbortReason() != null ? branchContext.getAbortReason()
                        : "并行分支已中断: " + elementId);
                break;
            }
        }
        log.debug("并行网关汇合完成: gatewayId={}", elementId);
    }

    @SuppressWarnings("unchecked")
    private C requireForkedContext(C parent) {
        FlowContext forked = parent.forkForParallelBranch();
        if (forked == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "forkForParallelBranch 返回 null: " + elementId);
        }
        if (forked == parent) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "forkForParallelBranch 不得返回父上下文同一实例: " + elementId);
        }
        if (!parent.getClass().isInstance(forked)) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "fork 类型必须与父上下文同类: expected=" + parent.getClass().getName() + ", actual="
                            + forked.getClass().getName());
        }
        return (C) forked;
    }

    private static void cancelAll(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            future.cancel(true);
        }
    }
}
