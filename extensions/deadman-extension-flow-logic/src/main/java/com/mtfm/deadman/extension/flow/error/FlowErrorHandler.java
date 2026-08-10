package com.mtfm.deadman.extension.flow.error;

import com.mtfm.deadman.extension.flow.FlowContext;

/**
 * 流程错误处理器：节点/元素执行抛错后的兜底钩子。
 * <p>
 * <strong>强制约定</strong>：{@link #handle} <em>必须</em>抛出异常（通常为业务异常）通知调用方。
 * 正常返回将被引擎视为编程错误并抛出 {@link com.mtfm.deadman.common.exception.BusinessException}，
 * 禁止「静默消化」导致调用方误判成功。
 * <p>
 * 示例：登录表单用户名含非法字符 → 抛出「用户名格式错误」。
 *
 * @param <C> 上下文类型
 */
@FunctionalInterface
public interface FlowErrorHandler<C extends FlowContext> {

    /**
     * 处理元素执行异常；必须抛出异常。
     *
     * @param event 错误事件（含元素 ID、上下文、原始异常）
     */
    void handle(FlowErrorEvent<C> event);
}
