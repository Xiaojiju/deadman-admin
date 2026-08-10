package com.mtfm.deadman.plugin.pay.constant;

/**
 * 商家转账额度默认值与可调范围（单位：分，对齐微信额度说明）。
 */
public final class TransferQuotaDefaults {

    /** 单笔默认 200 元 */
    public static final long SINGLE_DEFAULT = 20_000L;

    /** 单笔下限 0.1 元 */
    public static final long SINGLE_MIN = 10L;

    /** 单笔上限 200 元 */
    public static final long SINGLE_MAX = 20_000L;

    /** 非个体户：单用户日默认 2000 元 */
    public static final long NON_INDIVIDUAL_USER_DAILY_DEFAULT = 200_000L;

    /** 非个体户：单用户日上限 2000 元 */
    public static final long NON_INDIVIDUAL_USER_DAILY_MAX = 200_000L;

    /** 非个体户：单日默认 5 万元 */
    public static final long NON_INDIVIDUAL_DAILY_DEFAULT = 5_000_000L;

    /** 非个体户：单日上限 5 万元 */
    public static final long NON_INDIVIDUAL_DAILY_MAX = 5_000_000L;

    /** 非个体户：单月默认 3000 万元（不可调） */
    public static final long NON_INDIVIDUAL_MONTHLY = 3_000_000_000L;

    /** 个体户：单用户日默认/上限 200 元 */
    public static final long INDIVIDUAL_USER_DAILY_DEFAULT = 20_000L;

    public static final long INDIVIDUAL_USER_DAILY_MAX = 20_000L;

    /** 个体户：单日默认/上限 5000 元 */
    public static final long INDIVIDUAL_DAILY_DEFAULT = 500_000L;

    public static final long INDIVIDUAL_DAILY_MAX = 500_000L;

    /** 个体户：单月默认 10 万元（不可调） */
    public static final long INDIVIDUAL_MONTHLY = 10_000_000L;

    private TransferQuotaDefaults() {
    }
}
