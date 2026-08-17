package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 合单支付者信息。
 */
public class CombinePayerInfo {

    /** 用户在合单 AppId 下的 openid */
    private String openid;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
