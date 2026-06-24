package com.mtfm.deadman.plugin.logistics.spi.track;

/**
 * 物流轨迹查询入参。
 *
 * @param carrierCode 平台统一快递公司编码（见 {@link com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarriers}）
 * @param trackingNo  快递单号
 * @param phone       收/寄件人手机号后四位（顺丰等必填，可为 null）
 */
public record LogisticsTrackQueryContext(String carrierCode, String trackingNo, String phone) {
}
