package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 微信支付分取消服务订单请求。
 */
public class PayScoreCancelOrderRequest {

    /** 公众账号 ID */
    private String appid;
    /** 服务 ID */
    private String serviceId;
    /** 取消原因 */
    private String reason;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
