package com.mtfm.deadman.plugin.logistics.spi.ship;

import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;

/**
 * 商家寄件（官方快递）下单入参。
 *
 * @param carrierCode      平台统一快递公司编码
 * @param bizOrderId       业务侧订单号（对应快递100 thirdOrderId）
 * @param receiver         收件人
 * @param sender           寄件人
 * @param cargo            货物名称
 * @param weight           重量（kg 字符串）
 * @param remark           备注
 * @param serviceType      服务类型
 * @param dayType          预约日期类型
 * @param pickupStartTime  预约取件开始时间
 * @param pickupEndTime    预约取件结束时间
 * @param callbackUrl      下单回调地址
 * @param pollCallbackUrl  轨迹推送回调地址
 * @param payment          支付方式（默认 SHIPPER 寄付）
 */
public record LogisticsMerchantShipOrderContext(
        String carrierCode,
        String bizOrderId,
        LogisticsContactInfo receiver,
        LogisticsContactInfo sender,
        String cargo,
        String weight,
        String remark,
        String serviceType,
        String dayType,
        String pickupStartTime,
        String pickupEndTime,
        String callbackUrl,
        String pollCallbackUrl,
        String payment) {
}
