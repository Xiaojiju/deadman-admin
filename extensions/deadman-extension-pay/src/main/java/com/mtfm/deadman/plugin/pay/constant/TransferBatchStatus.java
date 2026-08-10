package com.mtfm.deadman.plugin.pay.constant;

/**
 * 商家转账批次状态。
 */
public final class TransferBatchStatus {

    /** 全部待转 */
    public static final String PENDING = "PENDING";

    /** 派发中（存在处理中明细） */
    public static final String DISPATCHING = "DISPATCHING";

    /** 部分成功 */
    public static final String PARTIAL_SUCCESS = "PARTIAL_SUCCESS";

    /** 全部成功 */
    public static final String SUCCESS = "SUCCESS";

    /** 全部失败 */
    public static final String FAILED = "FAILED";

    /** 人工停止派发（仍可能有待转） */
    public static final String STOPPED = "STOPPED";

    private TransferBatchStatus() {
    }
}
