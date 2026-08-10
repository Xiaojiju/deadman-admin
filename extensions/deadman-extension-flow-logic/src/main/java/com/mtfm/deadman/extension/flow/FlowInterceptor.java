package com.mtfm.deadman.extension.flow;

/**
 * 流程拦截器：在每个流程元素执行前后插入横切逻辑。
 * <p>
 * 典型用途：审计日志、风控校验、限流、耗时统计。
 * 默认方法为空实现，业务只需覆盖关心的回调。
 * <p>
 * 注意：拦截器在元素所属链的引擎循环中触发；嵌套子链内部元素由其自身引擎循环触发
 *（轻量子链默认无拦截器，仅父链对子链/网关这一层元素做 before/after）。
 *
 * @param <C> 上下文类型
 */
public interface FlowInterceptor<C extends FlowContext> {

    /**
     * 元素执行前回调。
     * <p>
     * 可在此根据上下文决定 {@link FlowContext#abort(String)}，引擎会跳过后续执行。
     *
     * @param elementId 即将执行的元素 ID
     * @param context 流程上下文
     */
    default void beforeElement(String elementId, C context) {
    }

    /**
     * 元素执行后回调（含网关路由、子链 execute 返回之后）。
     *
     * @param elementId 刚执行完的元素 ID
     * @param context 流程上下文
     */
    default void afterElement(String elementId, C context) {
    }
}
