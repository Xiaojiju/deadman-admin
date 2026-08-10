package com.mtfm.deadman.plugin.pay.service;

/**
 * 状态回写结果。
 * <p>
 * 仅当 {@link #applied()} 为 true 时表示本请求成功改写了状态，调用方才可触发累加金额、发布事件等副作用。
 *
 * @param applied        本请求是否成功改写状态
 * @param previousStatus 改写前状态（未改写时为当前状态）
 * @param currentStatus  改写后状态（未改写时与 previousStatus 相同）
 */
public record StatusTransitionResult(boolean applied, String previousStatus, String currentStatus) {

    /**
     * 未发生状态变更（幂等忽略、非法迁移、乐观锁冲突等）。
     *
     * @param status 当前状态
     * @return 结果
     */
    public static StatusTransitionResult notApplied(String status) {
        return new StatusTransitionResult(false, status, status);
    }

    /**
     * 本请求成功将状态从 previous 改为 current。
     *
     * @param previous 变更前状态
     * @param current  变更后状态
     * @return 结果
     */
    public static StatusTransitionResult applied(String previous, String current) {
        return new StatusTransitionResult(true, previous, current);
    }
}
