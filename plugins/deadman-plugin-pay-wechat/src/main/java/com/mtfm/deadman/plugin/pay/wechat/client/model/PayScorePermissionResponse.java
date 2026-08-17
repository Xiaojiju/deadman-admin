package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 微信支付分授权响应（创建预授权 / 按 openid 查询）。
 */
public class PayScorePermissionResponse {

    /** 服务 ID */
    private String serviceId;
    /** 公众账号 ID */
    private String appid;
    /** 用户标识 */
    private String openid;
    /** 授权协议号 */
    private String authorizationCode;
    /** 授权状态 */
    private String authorizationState;
    /** 预授权 token（创建预授权时返回） */
    private String applyPermissionsToken;
    /** 关联单号（可选） */
    private String openOrOrderId;

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

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getAuthorizationState() {
        return authorizationState;
    }

    public void setAuthorizationState(String authorizationState) {
        this.authorizationState = authorizationState;
    }

    public String getApplyPermissionsToken() {
        return applyPermissionsToken;
    }

    public void setApplyPermissionsToken(String applyPermissionsToken) {
        this.applyPermissionsToken = applyPermissionsToken;
    }

    public String getOpenOrOrderId() {
        return openOrOrderId;
    }

    public void setOpenOrOrderId(String openOrOrderId) {
        this.openOrOrderId = openOrOrderId;
    }
}
