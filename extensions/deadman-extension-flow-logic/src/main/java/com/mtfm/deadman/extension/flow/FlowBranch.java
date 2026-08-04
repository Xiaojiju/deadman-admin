package com.mtfm.deadman.extension.flow;

/**
 * 流程支流：挂在主节点之后执行（通知、积分、埋点等附加能力，不改变主链路契约）。
 *
 * @param <C> 上下文类型
 */
public interface FlowBranch<C extends FlowContext> {

    /**
     * 挂载的主节点 ID；主节点执行成功后触发本支流。
     *
     * @return 主节点 ID
     */
    String afterNodeId();

    /**
     * 支流执行顺序（升序，同 afterNodeId 内有效）。
     *
     * @return 排序号
     */
    default int order() {
        return 0;
    }

    /**
     * 执行支流逻辑。
     *
     * @param context 流程上下文
     */
    void execute(C context);
}
