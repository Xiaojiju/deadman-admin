package com.mtfm.deadman.plugin.pay.spi.transfer;

import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;

/**
 * 商家转账 Provider SPI。
 */
public interface TransferProvider {

    /**
     * 提供商标识，建议与支付 Provider 一致（如 wechat-jsapi / mock）。
     *
     * @return 提供商标识
     */
    String providerId();

    /**
     * 支付平台标识，如 {@code WECHAT} / {@code MOCK}。
     *
     * @return 支付平台
     */
    String payPlatform();

    /**
     * 是否支持指定标识。
     *
     * @param providerId 提供商标识
     * @return 是否支持
     */
    default boolean supports(String providerId) {
        return providerId().equals(providerId);
    }

    /**
     * 向渠道发起单笔转账。
     *
     * @param context   转账上下文
     * @param outBillNo 平台转账单号
     * @return 渠道受理结果
     */
    TransferResult createTransfer(TransferContext context, String outBillNo);

    /**
     * 解析转账结果回调。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    TransferNotifyResult parseTransferNotify(ChannelNotifyContext context);

    /**
     * 按平台转账单号查询渠道状态。
     *
     * @param outBillNo 平台转账单号
     * @return 查单结果
     */
    TransferQueryResult queryTransfer(String outBillNo);

    /**
     * 发起后是否由上层立即查单完成（Mock 可用）。
     *
     * @return 需要自动完成时返回 true
     */
    default boolean autoCompleteAfterTransfer() {
        return false;
    }
}
