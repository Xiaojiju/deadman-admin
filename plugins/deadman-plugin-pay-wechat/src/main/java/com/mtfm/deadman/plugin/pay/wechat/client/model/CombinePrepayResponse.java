package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 合单下单-JSAPI 响应。
 */
public class CombinePrepayResponse {

    /** 预支付交易会话标识 */
    private String prepayId;

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }
}
