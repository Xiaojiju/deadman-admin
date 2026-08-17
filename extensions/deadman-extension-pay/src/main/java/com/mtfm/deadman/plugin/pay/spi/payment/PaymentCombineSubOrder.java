package com.mtfm.deadman.plugin.pay.spi.payment;

/**
 * 合单支付子单。
 *
 * @param subMchid       二级商户号
 * @param outTradeNo     子单商户订单号
 * @param description    商品描述
 * @param amountTotal    子单金额（分）
 * @param profitSharing  是否分账冻结
 * @param attach         附加数据（可空）
 */
public record PaymentCombineSubOrder(
        String subMchid,
        String outTradeNo,
        String description,
        int amountTotal,
        boolean profitSharing,
        String attach) {

    /**
     * 构造默认无 attach 的子单。
     *
     * @param subMchid      二级商户号
     * @param outTradeNo    子单商户订单号
     * @param description   商品描述
     * @param amountTotal   子单金额（分）
     * @param profitSharing 是否分账冻结
     */
    public PaymentCombineSubOrder(
            String subMchid, String outTradeNo, String description, int amountTotal, boolean profitSharing) {
        this(subMchid, outTradeNo, description, amountTotal, profitSharing, null);
    }
}
