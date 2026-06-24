package com.mtfm.deadman.plugin.logistics.spi.waybill;

import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;

/**
 * 电子面单下单入参。
 *
 * @param carrierCode     快递公司编码
 * @param bizOrderId      业务侧订单号
 * @param receiver        收件人
 * @param sender          寄件人
 * @param cargo           货物名称
 * @param weight          重量（kg）
 * @param count           包裹数量
 * @param remark          备注
 * @param payType         支付方式（渠道枚举）
 * @param expType         快递类型（渠道枚举）
 * @param tempId          电子面单模板 ID
 * @param siid            云打印机设备码
 * @param needSubscribe   是否同时订阅轨迹推送
 * @param pollCallbackUrl 轨迹推送回调（needSubscribe 为 true 时生效）
 */
public record LogisticsWaybillOrderContext(
        String carrierCode,
        String bizOrderId,
        LogisticsContactInfo receiver,
        LogisticsContactInfo sender,
        String cargo,
        double weight,
        int count,
        String remark,
        String payType,
        String expType,
        String tempId,
        String siid,
        boolean needSubscribe,
        String pollCallbackUrl) {
}
