package com.mtfm.deadman.extension.flow.gateway;

import com.mtfm.deadman.extension.flow.FlowContext;

/**
 * 互斥网关分支条件函数。
 * <p>
 * 实现应根据 {@link FlowContext}（通常是业务子类）中的字段判断是否命中该分支，
 * 例如「订单商品是否为海鲜」「支付方式是否为微信」等。勿在条件中修改上下文产生副作用。
 *
 * @param <C> 上下文类型
 */
@FunctionalInterface
public interface FlowCondition<C extends FlowContext> {

    /**
     * 判断上下文是否满足本分支条件。
     *
     * @param context 流程上下文（只读使用为宜）
     * @return 满足则为 {@code true}，互斥网关将执行对应分支
     */
    boolean test(C context);
}
