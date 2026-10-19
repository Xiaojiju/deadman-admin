package com.mtfm.deadman.plugin.pay.support;

/**
 * 支付模块业务码。数值与历史接口保持一致，文案键仍为 result.PAY_*。
 */
public final class PayErrorCodes {

    /** 支付 Provider 不存在 */
    public static final int PAY_PROVIDER_NOT_FOUND = 14101;

    /** 支付单不存在 */
    public static final int PAY_ORDER_NOT_FOUND = 14102;

    /** 支付回调解析失败 */
    public static final int PAY_NOTIFY_PARSE_FAILED = 14103;

    /** 支付查单失败 */
    public static final int PAY_QUERY_FAILED = 14104;

    /** 退款 Provider 不存在 */
    public static final int PAY_REFUND_PROVIDER_NOT_FOUND = 14105;

    /** 退款单不存在 */
    public static final int PAY_REFUND_ORDER_NOT_FOUND = 14106;

    /** 当前支付单不可退款 */
    public static final int PAY_REFUND_NOT_ALLOWED = 14107;

    /** 退款金额不合法 */
    public static final int PAY_REFUND_AMOUNT_INVALID = 14108;

    /** 退款申请失败 */
    public static final int PAY_REFUND_FAILED = 14109;

    /** 退款回调解析失败 */
    public static final int PAY_REFUND_NOTIFY_PARSE_FAILED = 14110;

    /** 退款查单失败 */
    public static final int PAY_REFUND_QUERY_FAILED = 14111;

    /** 退款次数已达上限 */
    public static final int PAY_REFUND_LIMIT_EXCEEDED = 14112;

    /** 异常退款申请失败 */
    public static final int PAY_ABNORMAL_REFUND_FAILED = 14113;

    /** 当前退款单不可发起异常退款 */
    public static final int PAY_ABNORMAL_REFUND_NOT_ALLOWED = 14114;

    /** 转账 Provider 不存在 */
    public static final int PAY_TRANSFER_PROVIDER_NOT_FOUND = 14115;

    /** 转账批次不存在 */
    public static final int PAY_TRANSFER_BATCH_NOT_FOUND = 14116;

    /** 转账单不存在 */
    public static final int PAY_TRANSFER_BILL_NOT_FOUND = 14117;

    /** 转账金额不合法 */
    public static final int PAY_TRANSFER_AMOUNT_INVALID = 14118;

    /** 转账额度配置不合法 */
    public static final int PAY_TRANSFER_QUOTA_INVALID = 14119;

    /** 转账额度不足 */
    public static final int PAY_TRANSFER_QUOTA_EXCEEDED = 14120;

    /** 转账派发已停止 */
    public static final int PAY_TRANSFER_DISPATCH_STOPPED = 14121;

    /** 转账申请失败 */
    public static final int PAY_TRANSFER_FAILED = 14122;

    /** 转账回调解析失败 */
    public static final int PAY_TRANSFER_NOTIFY_PARSE_FAILED = 14123;

    /** 转账金额与本地不一致 */
    public static final int PAY_TRANSFER_AMOUNT_MISMATCH = 14124;

    /** 转账创建超出平台限额 */
    public static final int PAY_TRANSFER_LIMIT_EXCEEDED = 14125;

    /** 转账本地终态与渠道结果冲突 */
    public static final int PAY_TRANSFER_STATUS_CONFLICT = 14126;

    /** 转账业务单号已存在但关键参数不一致 */
    public static final int PAY_TRANSFER_IDEMPOTENT_CONFLICT = 14127;

    /** 支付金额与本地不一致 */
    public static final int PAY_AMOUNT_MISMATCH = 14128;

    /** 退款金额或支付单号与本地不一致 */
    public static final int PAY_REFUND_AMOUNT_MISMATCH = 14129;

    /** 支付资金链路与门面不匹配 */
    public static final int PAY_FUND_LANE_MISMATCH = 14130;

    /** 资金账户与业务场景不匹配 */
    public static final int PAY_FUND_ACCOUNT_MISMATCH = 14131;

    /** 二级商户 Provider 不存在 */
    public static final int PAY_SUB_MERCHANT_PROVIDER_NOT_FOUND = 14132;

    /** 二级商户进件失败 */
    public static final int PAY_SUB_MERCHANT_APPLY_FAILED = 14133;

    /** 支付渠道媒体文件上传失败 */
    public static final int PAY_MEDIA_UPLOAD_FAILED = 14134;

    /** 支付分 Provider 不存在 */
    public static final int PAY_SCORE_PROVIDER_NOT_FOUND = 14135;

    /** 支付分回调解析失败 */
    public static final int PAY_SCORE_NOTIFY_PARSE_FAILED = 14136;

    private PayErrorCodes() {}
}
