package com.mtfm.deadman.plugin.logistics.vo;

/**
 * 可供选择的快递公司（平台统一编码 + 展示名称）。
 *
 * @param carrierCode 平台统一快递公司编码（如 {@code YTO}、{@code SF}）
 * @param carrierName 快递公司中文名称
 */
public record LogisticsCarrierOptionVO(String carrierCode, String carrierName) {
}
