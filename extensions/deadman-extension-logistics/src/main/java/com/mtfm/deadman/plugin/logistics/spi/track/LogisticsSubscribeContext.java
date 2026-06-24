package com.mtfm.deadman.plugin.logistics.spi.track;

/**
 * 物流轨迹订阅入参。
 *
 * @param carrierCode 快递公司编码
 * @param trackingNo  快递单号
 * @param phone       收/寄件人手机号后四位（可选）
 * @param fromAddress 出发地（可选，提升准确率）
 * @param toAddress   目的地（可选）
 * @param callbackUrl 推送回调地址，为空时使用插件默认
 */
public record LogisticsSubscribeContext(
        String carrierCode,
        String trackingNo,
        String phone,
        String fromAddress,
        String toAddress,
        String callbackUrl) {
}
