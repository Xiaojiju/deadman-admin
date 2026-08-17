package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 微信支付分服务订单响应（创建/查询/取消/完结共用关键字段）。
 */
public class PayScoreServiceOrderResponse {

    /** 商户服务订单号 */
    private String outOrderNo;
    /** 微信支付服务订单号 */
    private String orderId;
    /** 服务 ID */
    private String serviceId;
    /** 公众账号 ID */
    private String appid;
    /** 用户标识 */
    private String openid;
    /** 服务订单状态 */
    private String state;
    /** 订单状态说明 */
    private String stateDescription;
    /** 总金额（分） */
    private Long totalAmount;
    /** 调起确认 package */
    private String packageInfo;

    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(String packageInfo) {
        this.packageInfo = packageInfo;
    }
}
