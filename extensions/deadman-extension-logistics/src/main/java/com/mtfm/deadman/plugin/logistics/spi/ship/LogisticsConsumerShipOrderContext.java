package com.mtfm.deadman.plugin.logistics.spi.ship;

import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;

/**
 * C 端寄件下单入参。
 *
 * @param carrierCode     快递公司编码
 * @param receiver        收件人
 * @param sender          寄件人
 * @param cargo           货物名称
 * @param weight          重量
 * @param remark          备注
 * @param dayType         预约日期类型
 * @param pickupStartTime 预约取件开始时间
 * @param pickupEndTime   预约取件结束时间
 * @param callbackUrl     下单回调地址
 * @param payment         支付方式
 * @param expType         快递类型
 */
public record LogisticsConsumerShipOrderContext(
        String carrierCode,
        LogisticsContactInfo receiver,
        LogisticsContactInfo sender,
        String cargo,
        String weight,
        String remark,
        String dayType,
        String pickupStartTime,
        String pickupEndTime,
        String callbackUrl,
        String payment,
        String expType) {
}
