package com.mtfm.deadman.plugin.pay.service;

/**
 * 待转明细抢占（含额度闸门）结果。
 */
public enum TransferClaimOutcome {

    /** 已抢占为 PROCESSING，可发起渠道 */
    CLAIMED,

    /** 全局日额或月额不足，本轮应停止派发 */
    GLOBAL_QUOTA_EXCEEDED,

    /** 单用户日额不足，应跳过该用户继续后续 */
    USER_DAILY_QUOTA_EXCEEDED,

    /** 全局派发已停止 */
    DISPATCH_DISABLED,

    /** 单据不可抢占（非 PENDING / 批次已停止 / CAS 失败） */
    NOT_CLAIMABLE
}
