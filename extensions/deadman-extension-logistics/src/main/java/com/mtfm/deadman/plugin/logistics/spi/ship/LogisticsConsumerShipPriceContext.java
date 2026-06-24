package com.mtfm.deadman.plugin.logistics.spi.ship;

import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;

/**
 * C 端寄件价格查询入参。
 *
 * @param carrierCode     快递公司编码
 * @param sender          寄件人
 * @param receiver        收件人
 * @param weight          重量
 * @param serviceType     服务类型
 */
public record LogisticsConsumerShipPriceContext(
        String carrierCode,
        LogisticsContactInfo sender,
        LogisticsContactInfo receiver,
        String weight,
        String serviceType) {
}
