package com.mtfm.deadman.plugin.pay.dto.transfer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 更新商家转账额度配置。
 *
 * @param merchantEntityType   商户主体：NON_INDIVIDUAL / INDIVIDUAL
 * @param singleLimitCents     单笔限额（分）
 * @param userDailyLimitCents  单用户单日限额（分）
 * @param dailyLimitCents      单日总额度（分）
 */
public record UpdateTransferQuotaRequest(
        @NotBlank String merchantEntityType,
        @NotNull @Min(10) Long singleLimitCents,
        @NotNull @Min(10) Long userDailyLimitCents,
        @NotNull @Min(10) Long dailyLimitCents) {
}
