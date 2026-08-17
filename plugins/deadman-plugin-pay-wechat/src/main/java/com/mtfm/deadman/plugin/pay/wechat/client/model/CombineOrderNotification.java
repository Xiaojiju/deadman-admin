package com.mtfm.deadman.plugin.pay.wechat.client.model;

import java.util.List;

/**
 * 合单查单/支付通知资源体。
 */
public class CombineOrderNotification {

    /** 合单商户订单号 */
    private String combineOutTradeNo;
    /** 合单交易状态（部分场景） */
    private String tradeState;
    /** 子单列表 */
    private List<CombineSubOrderResult> subOrders;

    public String getCombineOutTradeNo() {
        return combineOutTradeNo;
    }

    public void setCombineOutTradeNo(String combineOutTradeNo) {
        this.combineOutTradeNo = combineOutTradeNo;
    }

    public String getTradeState() {
        return tradeState;
    }

    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

    public List<CombineSubOrderResult> getSubOrders() {
        return subOrders;
    }

    public void setSubOrders(List<CombineSubOrderResult> subOrders) {
        this.subOrders = subOrders;
    }
}
