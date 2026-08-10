package com.mtfm.deadman.extension.flow.event;

import com.mtfm.deadman.extension.flow.FlowContext;

/**
 * 节点执行成功事件，携带当时的流程上下文快照引用（同一实例，非深拷贝）。
 *
 * @param elementId 成功节点 ID
 * @param context 流程上下文
 * @param <C> 上下文类型
 */
public record FlowNodeSuccessEvent<C extends FlowContext>(
    /** 成功节点 ID */
    String elementId,
    /** 流程上下文（与执行链共享同一实例） */
    C context) {
}
