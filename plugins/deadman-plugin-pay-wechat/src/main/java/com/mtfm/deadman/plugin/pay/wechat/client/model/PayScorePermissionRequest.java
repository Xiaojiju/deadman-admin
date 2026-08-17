package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 微信支付分授权相关请求体（创建预授权 / 解除授权）。
 */
public class PayScorePermissionRequest {

    /** 服务 ID */
    private String serviceId;
    /** 公众账号 ID */
    private String appid;
    /** 授权协议号 */
    private String authorizationCode;
    /** 授权结果回调地址 */
    private String notifyUrl;
    /** 解除授权原因 */
    private String reason;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
