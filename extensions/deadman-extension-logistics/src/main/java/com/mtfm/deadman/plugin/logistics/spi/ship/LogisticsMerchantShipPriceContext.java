package com.mtfm.deadman.plugin.logistics.spi.ship;

import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;

/**
 * 商家寄件（官方快递）价格查询入参。
 *
 * @param carrierCode 平台统一快递公司编码
 * @param sender      寄件人
 * @param receiver    收件人
 * @param weight      重量
 * @param serviceType 服务类型
 */
public record LogisticsMerchantShipPriceContext(
        String carrierCode,
        LogisticsContactInfo sender,
        LogisticsContactInfo receiver,
        String weight,
        String serviceType) {
}
