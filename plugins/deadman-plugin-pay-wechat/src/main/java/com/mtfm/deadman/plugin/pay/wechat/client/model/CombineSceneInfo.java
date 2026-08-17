package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 合单场景信息。
 */
public class CombineSceneInfo {

    /** 用户终端 IP */
    private String payerClientIp;

    public String getPayerClientIp() {
        return payerClientIp;
    }

    public void setPayerClientIp(String payerClientIp) {
        this.payerClientIp = payerClientIp;
    }
}
