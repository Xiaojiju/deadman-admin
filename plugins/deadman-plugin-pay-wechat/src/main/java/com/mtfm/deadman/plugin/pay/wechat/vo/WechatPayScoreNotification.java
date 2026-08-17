package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信支付分结果通知资源（验签解密后；字段命名与 SDK Gson 下划线策略对齐）。
 */
public class WechatPayScoreNotification {

    /** 商户服务订单号 */
    private String outOrderNo;
    /** 微信支付服务订单号 */
    private String orderId;
    /** 服务订单状态 */
    private String state;
    /** 订单状态说明 */
    private String stateDescription;
    /** 总金额（分） */
    private Long totalAmount;
    /** 用户标识 */
    private String openid;
    /** 调起确认 package */
    private String packageInfo;
    /** 事件类型 */
    private String eventType;

    /**
     * @return 商户服务订单号
     */
    public String getOutOrderNo() {
        return outOrderNo;
    }

    /**
     * @param outOrderNo 商户服务订单号
     */
    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    /**
     * @return 微信服务订单号
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @param orderId 微信服务订单号
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return 状态
     */
    public String getState() {
        return state;
    }

    /**
     * @param state 状态
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return 状态描述
     */
    public String getStateDescription() {
        return stateDescription;
    }

    /**
     * @param stateDescription 状态描述
     */
    public void setStateDescription(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    /**
     * @return 总金额（分）
     */
    public Long getTotalAmount() {
        return totalAmount;
    }

    /**
     * @param totalAmount 总金额（分）
     */
    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * @return openid
     */
    public String getOpenid() {
        return openid;
    }

    /**
     * @param openid openid
     */
    public void setOpenid(String openid) {
        this.openid = openid;
    }

    /**
     * @return package
     */
    public String getPackageInfo() {
        return packageInfo;
    }

    /**
     * @param packageInfo package
     */
    public void setPackageInfo(String packageInfo) {
        this.packageInfo = packageInfo;
    }

    /**
     * @return 事件类型
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @param eventType 事件类型
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
