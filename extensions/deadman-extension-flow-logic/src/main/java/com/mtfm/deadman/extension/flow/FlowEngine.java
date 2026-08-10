package com.mtfm.deadman.extension.flow;

import java.util.List;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.extension.flow.error.FlowErrorEvent;
import com.mtfm.deadman.extension.flow.error.FlowErrorHandler;
import com.mtfm.deadman.extension.flow.event.FlowNodeEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 流程引擎：顺序推进元素，统一处理拦截器、错误兜底与节点成功事件。
 * <p>
 * 执行顺序：before → run → after →（节点）成功事件；拦截器异常同样进入错误处理器。
 */
@Slf4j
final class FlowEngine {

    private FlowEngine() {
    }

    /**
     * 顺序执行元素列表。
     *
     * @param chainId 链标识
     * @param elements 有序元素
     * @param interceptors 拦截器
     * @param errorHandler 错误处理器（可为 null）
     * @param eventListeners 节点成功监听器
     * @param context 上下文
     * @param <C> 上下文类型
     */
    static <C extends FlowContext> void execute(String chainId, List<? extends FlowElement<C>> elements,
            List<? extends FlowInterceptor<? super C>> interceptors, FlowErrorHandler<? super C> errorHandler,
            List<? extends FlowNodeEventListener<? super C>> eventListeners, C context) {
        List<? extends FlowNodeEventListener<? super C>> listeners =
                eventListeners == null ? List.of() : eventListeners;
        for (FlowElement<C> element : elements) {
            if (context.isAborted()) {
                log.info("流程已中断，停止推进: chainId={}, reason={}", chainId, context.getAbortReason());
                return;
            }
            String elementId = element.elementId();
            log.debug("执行流程元素: chainId={}, elementId={}, type={}", chainId, elementId,
                    element.getClass().getSimpleName());
            boolean stopCurrentChain = false;
            try {
                for (FlowInterceptor<? super C> interceptor : interceptors) {
                    interceptor.beforeElement(elementId, context);
                }
                if (context.isAborted()) {
                    return;
                }
                stopCurrentChain = element.run(context);
                if (context.isAborted()) {
                    return;
                }
                for (FlowInterceptor<? super C> interceptor : interceptors) {
                    interceptor.afterElement(elementId, context);
                }
                // 成功事件在 after 之后，避免 after 失败却已对外宣告成功
                if (element.notifiableNode() && !context.isAborted()) {
                    notifySuccess(listeners, elementId, context);
                }
            } catch (Throwable error) {
                handleError(chainId, elementId, context, error, errorHandler);
                return;
            }
            if (stopCurrentChain || context.isAborted()) {
                return;
            }
        }
    }

    private static <C extends FlowContext> void notifySuccess(
            List<? extends FlowNodeEventListener<? super C>> listeners, String elementId, C context) {
        if (listeners.isEmpty()) {
            return;
        }
        for (FlowNodeEventListener<? super C> listener : listeners) {
            try {
                listener.onNodeSuccess(elementId, context);
            } catch (RuntimeException ex) {
                log.warn("节点成功事件监听失败: elementId={}, listener={}", elementId,
                        listener.getClass().getSimpleName(), ex);
            }
        }
    }

    /**
     * 错误兜底：有 handler 则调用且<strong>要求其抛出</strong>；无 handler 则包装为 {@link FlowElementException} 抛出。
     */
    private static <C extends FlowContext> void handleError(String chainId, String elementId, C context,
            Throwable error, FlowErrorHandler<? super C> errorHandler) {
        String reportId = FlowElementException.resolveElementId(error, elementId);
        log.debug("流程元素执行失败: chainId={}, elementId={}, reportId={}, error={}", chainId, elementId, reportId,
                error.toString());
        if (errorHandler != null) {
            errorHandler.handle(new FlowErrorEvent<>(reportId, context, error));
            // 契约：handler 必须抛异常；若落到此处视为编程错误
            throw new BusinessException(ResultCode.INTERNAL_ERROR,
                    "FlowErrorHandler 必须抛出异常以通知调用方: " + reportId);
        }
        throw FlowElementException.wrap(reportId, error);
    }
}
