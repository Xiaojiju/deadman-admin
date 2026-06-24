package com.mtfm.deadman.plugin.logistics.spi.carrier;

/**
 * 快递公司智能识别结果。
 *
 * @param carrierCode 平台统一快递公司编码（见 {@link com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarriers}）
 * @param carrierName 快递公司名称
 * @param lengthPre   单号长度前缀规则
 */
public record LogisticsCarrierDetectResult(String carrierCode, String carrierName, String lengthPre) {
}
