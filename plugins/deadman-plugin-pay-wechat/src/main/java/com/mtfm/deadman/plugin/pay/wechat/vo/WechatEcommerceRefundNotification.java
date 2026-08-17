package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通退款结果通知解密后的资源对象（与国内 {@code RefundNotification} 字段不完全相同）。
 * <p>
 * 字段命名与微信 SDK Gson 下划线策略对齐：{@code refundStatus} ↔ {@code refund_status}。
 */
public class WechatEcommerceRefundNotification {

    /** 服务商商户号 */
    private String spMchid;

    /** 二级商户号 */
    private String subMchid;

    /** 微信支付订单号 */
    private String transactionId;

    /** 商户订单号 */
    private String outTradeNo;

    /** 微信退款单号 */
    private String refundId;

    /** 商户退款单号 */
    private String outRefundNo;

    /** 退款状态：SUCCESS/CLOSED/PROCESSING/ABNORMAL */
    private String refundStatus;

    /** 退款入账账户 */
    private String userReceivedAccount;

    /** 金额 */
    private Amount amount;

    /**
     * @return 服务商商户号
     */
    public String getSpMchid() {
        return spMchid;
    }

    /**
     * @param spMchid 服务商商户号
     */
    public void setSpMchid(String spMchid) {
        this.spMchid = spMchid;
    }

    /**
     * @return 二级商户号
     */
    public String getSubMchid() {
        return subMchid;
    }

    /**
     * @param subMchid 二级商户号
     */
    public void setSubMchid(String subMchid) {
        this.subMchid = subMchid;
    }

    /**
     * @return 微信支付订单号
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @param transactionId 微信支付订单号
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * @return 商户订单号
     */
    public String getOutTradeNo() {
        return outTradeNo;
    }

    /**
     * @param outTradeNo 商户订单号
     */
    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    /**
     * @return 微信退款单号
     */
    public String getRefundId() {
        return refundId;
    }

    /**
     * @param refundId 微信退款单号
     */
    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    /**
     * @return 商户退款单号
     */
    public String getOutRefundNo() {
        return outRefundNo;
    }

    /**
     * @param outRefundNo 商户退款单号
     */
    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    /**
     * @return 退款状态
     */
    public String getRefundStatus() {
        return refundStatus;
    }

    /**
     * @param refundStatus 退款状态
     */
    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    /**
     * @return 退款入账账户
     */
    public String getUserReceivedAccount() {
        return userReceivedAccount;
    }

    /**
     * @param userReceivedAccount 退款入账账户
     */
    public void setUserReceivedAccount(String userReceivedAccount) {
        this.userReceivedAccount = userReceivedAccount;
    }

    /**
     * @return 金额
     */
    public Amount getAmount() {
        return amount;
    }

    /**
     * @param amount 金额
     */
    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    /**
     * 退款通知金额。
     */
    public static class Amount {

        /** 订单总金额（分） */
        private Integer total;

        /** 退款金额（分） */
        private Integer refund;

        /**
         * @return 订单总金额（分）
         */
        public Integer getTotal() {
            return total;
        }

        /**
         * @param total 订单总金额（分）
         */
        public void setTotal(Integer total) {
            this.total = total;
        }

        /**
         * @return 退款金额（分）
         */
        public Integer getRefund() {
            return refund;
        }

        /**
         * @param refund 退款金额（分）
         */
        public void setRefund(Integer refund) {
            this.refund = refund;
        }
    }
}
