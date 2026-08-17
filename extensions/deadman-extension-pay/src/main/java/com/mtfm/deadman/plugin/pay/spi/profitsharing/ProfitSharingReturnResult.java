package com.mtfm.deadman.plugin.pay.spi.profitsharing;

/**
 * 分账回退结果。
 *
 * @param outReturnNo      平台回退单号
 * @param channelReturnId  微信回退单号
 * @param status           渠道状态
 */
public record ProfitSharingReturnResult(String outReturnNo, String channelReturnId, String status) {
}
