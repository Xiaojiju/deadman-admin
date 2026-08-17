package com.mtfm.deadman.plugin.pay.wechat.client.model;

import java.util.List;

/**
 * 微信支付分服务订单创建请求（字段命名与 SDK Gson 下划线策略对齐）。
 */
public class PayScoreServiceOrderRequest {

    /** 商户服务订单号 */
    private String outOrderNo;
    /** 公众账号 ID */
    private String appid;
    /** 服务 ID */
    private String serviceId;
    /** 服务信息 */
    private String serviceIntroduction;
    /** 后付费项目 */
    private List<PayScorePostPaymentItem> postPayments;
    /** 风险金 */
    private PayScoreRiskFund riskFund;
    /** 服务时间段 */
    private PayScoreTimeRange timeRange;
    /** 服务位置 */
    private PayScoreLocation location;
    /** 用户标识 */
    private String openid;
    /** 是否需要用户确认 */
    private Boolean needUserConfirm;
    /** 商户回调地址 */
    private String notifyUrl;
    /** 商户数据包 */
    private String attach;

    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

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

    public String getServiceIntroduction() {
        return serviceIntroduction;
    }

    public void setServiceIntroduction(String serviceIntroduction) {
        this.serviceIntroduction = serviceIntroduction;
    }

    public List<PayScorePostPaymentItem> getPostPayments() {
        return postPayments;
    }

    public void setPostPayments(List<PayScorePostPaymentItem> postPayments) {
        this.postPayments = postPayments;
    }

    public PayScoreRiskFund getRiskFund() {
        return riskFund;
    }

    public void setRiskFund(PayScoreRiskFund riskFund) {
        this.riskFund = riskFund;
    }

    public PayScoreTimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(PayScoreTimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public PayScoreLocation getLocation() {
        return location;
    }

    public void setLocation(PayScoreLocation location) {
        this.location = location;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Boolean getNeedUserConfirm() {
        return needUserConfirm;
    }

    public void setNeedUserConfirm(Boolean needUserConfirm) {
        this.needUserConfirm = needUserConfirm;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    /**
     * 风险金。
     */
    public static class PayScoreRiskFund {

        /** 风险金名称 */
        private String name;
        /** 风险金额（分） */
        private Long amount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }
    }

    /**
     * 服务时间段。
     */
    public static class PayScoreTimeRange {

        /** 开始时间 */
        private String startTime;
        /** 结束时间 */
        private String endTime;

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

    /**
     * 服务位置。
     */
    public static class PayScoreLocation {

        /** 位置名称 */
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 后付费/优惠项目。
     */
    public static class PayScorePostPaymentItem {

        /** 名称 */
        private String name;
        /** 金额（分） */
        private Long amount;
        /** 说明 */
        private String description;
        /** 数量 */
        private Integer count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}
