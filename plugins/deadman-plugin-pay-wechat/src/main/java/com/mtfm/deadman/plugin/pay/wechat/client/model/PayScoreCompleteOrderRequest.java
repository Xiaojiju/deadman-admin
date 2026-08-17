package com.mtfm.deadman.plugin.pay.wechat.client.model;

import java.util.List;

/**
 * 微信支付分完结服务订单请求。
 */
public class PayScoreCompleteOrderRequest {

    /** 公众账号 ID */
    private String appid;
    /** 服务 ID */
    private String serviceId;
    /** 后付费项目 */
    private List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> postPayments;
    /** 总金额（分） */
    private Long totalAmount;
    /** 优惠项目 */
    private List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> postDiscounts;
    /** 服务时间段 */
    private PayScoreServiceOrderRequest.PayScoreTimeRange timeRange;
    /** 完结时间 */
    private String completeTime;

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

    public List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> getPostPayments() {
        return postPayments;
    }

    public void setPostPayments(List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> postPayments) {
        this.postPayments = postPayments;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> getPostDiscounts() {
        return postDiscounts;
    }

    public void setPostDiscounts(List<PayScoreServiceOrderRequest.PayScorePostPaymentItem> postDiscounts) {
        this.postDiscounts = postDiscounts;
    }

    public PayScoreServiceOrderRequest.PayScoreTimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(PayScoreServiceOrderRequest.PayScoreTimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }
}
