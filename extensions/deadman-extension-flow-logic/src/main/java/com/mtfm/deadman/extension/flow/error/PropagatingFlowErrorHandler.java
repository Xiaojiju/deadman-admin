package com.mtfm.deadman.extension.flow.error;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.extension.flow.FlowContext;
import com.mtfm.deadman.extension.flow.FlowElementException;

/**
 * 默认错误处理器：优先透传 {@link BusinessException}，其余包装为带 cause 的内部错误。
 *
 * @param <C> 上下文类型
 */
public final class PropagatingFlowErrorHandler<C extends FlowContext> implements FlowErrorHandler<C> {

    /** 单例（无状态，可安全共享） */
    public static final PropagatingFlowErrorHandler<FlowContext> INSTANCE = new PropagatingFlowErrorHandler<>();

    /**
     * @param <C> 上下文类型
     * @return 共享实例（泛型擦除下安全）
     */
    @SuppressWarnings("unchecked")
    public static <C extends FlowContext> FlowErrorHandler<C> instance() {
        return (FlowErrorHandler<C>) INSTANCE;
    }

    @Override
    public void handle(FlowErrorEvent<C> event) {
        Throwable error = event.error();
        Throwable root = error instanceof FlowElementException fee && fee.getCause() != null ? fee.getCause() : error;
        if (root instanceof BusinessException business) {
            throw business;
        }
        if (root instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (root instanceof Error fatal) {
            throw fatal;
        }
        throw new BusinessException(ResultCode.INTERNAL_ERROR, "流程执行失败: " + event.elementId(), root);
    }
}
