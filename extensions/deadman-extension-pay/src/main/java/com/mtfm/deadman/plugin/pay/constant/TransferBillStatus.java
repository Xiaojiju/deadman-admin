package com.mtfm.deadman.plugin.pay.constant;

/**
 * 商家转账明细状态（对齐微信商家转账 state，并扩展 PENDING 待转）。
 */
public final class TransferBillStatus {

    /** 待转（额度不足或等待派发） */
    public static final String PENDING = "PENDING";

    /** 已受理 */
    public static final String ACCEPTED = "ACCEPTED";

    /** 处理中（锁定资金等） */
    public static final String PROCESSING = "PROCESSING";

    /** 待用户确认收款 */
    public static final String WAIT_USER_CONFIRM = "WAIT_USER_CONFIRM";

    /** 转账中 */
    public static final String TRANSFERING = "TRANSFERING";

    /** 成功 */
    public static final String SUCCESS = "SUCCESS";

    /** 失败 */
    public static final String FAIL = "FAIL";

    /** 已撤销/取消 */
    public static final String CANCELLED = "CANCELLED";

    private TransferBillStatus() {
    }

    /**
     * 是否占用额度的在途/成功状态（不含 PENDING）。
     *
     * @param status 状态
     * @return 是否计入额度
     */
    public static boolean countsTowardQuota(String status) {
        return ACCEPTED.equals(status)
                || PROCESSING.equals(status)
                || WAIT_USER_CONFIRM.equals(status)
                || TRANSFERING.equals(status)
                || SUCCESS.equals(status);
    }

    /**
     * 是否硬终态。
     *
     * @param status 状态
     * @return 是否终态
     */
    public static boolean isTerminal(String status) {
        return SUCCESS.equals(status) || FAIL.equals(status) || CANCELLED.equals(status);
    }
}
