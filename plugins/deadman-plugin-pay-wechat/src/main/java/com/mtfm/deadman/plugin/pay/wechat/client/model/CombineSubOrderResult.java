package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 合单子单查单/通知结果。
 */
public class CombineSubOrderResult {

    /** 微信支付订单号 */
    private String transactionId;
    /** 交易状态 */
    private String tradeState;
    /** 金额 */
    private CombineAmount amount;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTradeState() {
        return tradeState;
    }

    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

    public CombineAmount getAmount() {
        return amount;
    }

    public void setAmount(CombineAmount amount) {
        this.amount = amount;
    }
}
