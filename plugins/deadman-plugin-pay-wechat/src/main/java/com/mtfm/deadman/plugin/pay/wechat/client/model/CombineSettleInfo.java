package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 合单子单结算信息。
 */
public class CombineSettleInfo {

    /** 是否分账 */
    private Boolean profitSharing;

    public Boolean getProfitSharing() {
        return profitSharing;
    }

    public void setProfitSharing(Boolean profitSharing) {
        this.profitSharing = profitSharing;
    }
}
