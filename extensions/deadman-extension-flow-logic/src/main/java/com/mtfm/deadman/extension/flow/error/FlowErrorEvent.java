package com.mtfm.deadman.extension.flow.error;

import com.mtfm.deadman.extension.flow.FlowContext;

/**
 * 流程元素执行失败时的错误事件，供 {@link FlowErrorHandler} 兜底处理。
 *
 * @param elementId 出错元素 ID（节点/网关/子链）
 * @param context 当前流程上下文（可读取业务字段以组装友好错误）
 * @param error 原始异常
 * @param <C> 上下文类型
 */
public record FlowErrorEvent<C extends FlowContext>(
    /** 出错元素 ID */
    String elementId,
    /** 流程上下文 */
    C context,
    /** 原始异常 */
    Throwable error) {
}
