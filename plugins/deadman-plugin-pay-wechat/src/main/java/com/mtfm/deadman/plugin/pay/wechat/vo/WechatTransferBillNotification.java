package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信商家转账结果通知资源（验签解密后；字段命名与 SDK Gson 下划线策略对齐）。
 */
public class WechatTransferBillNotification {

    private String outBillNo;
    private String transferBillNo;
    private String state;
    private Long transferAmount;
    private String failReason;
    private String packageInfo;

    /**
     * @return 商户转账单号
     */
    public String getOutBillNo() {
        return outBillNo;
    }

    /**
     * @param outBillNo 商户转账单号
     */
    public void setOutBillNo(String outBillNo) {
        this.outBillNo = outBillNo;
    }

    /**
     * @return 微信转账单号
     */
    public String getTransferBillNo() {
        return transferBillNo;
    }

    /**
     * @param transferBillNo 微信转账单号
     */
    public void setTransferBillNo(String transferBillNo) {
        this.transferBillNo = transferBillNo;
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
     * @return 金额（分）
     */
    public Long getTransferAmount() {
        return transferAmount;
    }

    /**
     * @param transferAmount 金额（分）
     */
    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }

    /**
     * @return 失败原因
     */
    public String getFailReason() {
        return failReason;
    }

    /**
     * @param failReason 失败原因
     */
    public void setFailReason(String failReason) {
        this.failReason = failReason;
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
}
