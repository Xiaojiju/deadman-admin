package com.mtfm.deadman.plugin.pay.vo.transfer;

/**
 * 转账额度配置与当前用量。
 *
 * @param merchantEntityType      商户主体
 * @param singleLimitCents        单笔限额（分）
 * @param userDailyLimitCents     单用户单日限额（分）
 * @param dailyLimitCents         单日总额度（分）
 * @param monthlyLimitCents       单月总额度（分，不可调）
 * @param dispatchEnabled         是否允许派发
 * @param usedDailyCents          今日已占用（分）
 * @param usedMonthlyCents        本月已占用（分）
 * @param singleMinCents          单笔可调下限
 * @param singleMaxCents          单笔可调上限
 * @param userDailyMaxCents       单用户日可调上限
 * @param dailyMaxCents           单日可调上限
 */
public record TransferQuotaVO(
        String merchantEntityType,
        Long singleLimitCents,
        Long userDailyLimitCents,
        Long dailyLimitCents,
        Long monthlyLimitCents,
        Boolean dispatchEnabled,
        Long usedDailyCents,
        Long usedMonthlyCents,
        Long singleMinCents,
        Long singleMaxCents,
        Long userDailyMaxCents,
        Long dailyMaxCents) {
}
