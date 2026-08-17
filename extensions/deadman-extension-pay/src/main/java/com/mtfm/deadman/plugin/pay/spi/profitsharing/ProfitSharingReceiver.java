package com.mtfm.deadman.plugin.pay.spi.profitsharing;

/**
 * 分账接收方（订单分账阶段仅允许 MERCHANT_ID）。
 *
 * @param type        接收方类型，合规场景固定为 MERCHANT_ID
 * @param account     接收方商户号（服务商 partner_mchid）
 * @param amountCents 分账金额（分）
 * @param description 分账描述
 * @param purpose     业务用途编码：PLATFORM_FEE / STAFF_TRANSIT
 */
public record ProfitSharingReceiver(
        String type,
        String account,
        int amountCents,
        String description,
        String purpose) {

    /** 接收方类型：商户号 */
    public static final String TYPE_MERCHANT_ID = "MERCHANT_ID";

    /** 用途：平台服务费 */
    public static final String PURPOSE_PLATFORM_FEE = "PLATFORM_FEE";

    /** 用途：员工中转佣金 */
    public static final String PURPOSE_STAFF_TRANSIT = "STAFF_TRANSIT";
}
