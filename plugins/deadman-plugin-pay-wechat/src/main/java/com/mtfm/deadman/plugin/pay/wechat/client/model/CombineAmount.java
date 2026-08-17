package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 合单子单金额。
 */
public class CombineAmount {

    /** 总金额（分），合单下单/部分通知字段为 total_amount */
    private Integer totalAmount;
    /** 部分查单/通知场景使用的金额字段 total */
    private Integer total;
    /** 货币类型，固定 CNY */
    private String currency;

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * 解析可用金额（优先 total_amount）。
     *
     * @return 金额（分），均无则 null
     */
    public Integer resolveTotal() {
        return totalAmount != null ? totalAmount : total;
    }
}
