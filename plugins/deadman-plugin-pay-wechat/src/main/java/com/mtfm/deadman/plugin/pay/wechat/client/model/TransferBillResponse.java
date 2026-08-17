package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 商家转账到零钱申请/查单响应。
 */
public class TransferBillResponse {

    /** 商户转账单号 */
    private String outBillNo;
    /** 微信转账单号 */
    private String transferBillNo;
    /** 单据状态 */
    private String state;
    /** 转账金额（分） */
    private Long transferAmount;
    /** 失败原因 */
    private String failReason;
    /** 跳转领取页面 package 信息 */
    private String packageInfo;

    public String getOutBillNo() {
        return outBillNo;
    }

    public void setOutBillNo(String outBillNo) {
        this.outBillNo = outBillNo;
    }

    public String getTransferBillNo() {
        return transferBillNo;
    }

    public void setTransferBillNo(String transferBillNo) {
        this.transferBillNo = transferBillNo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(String packageInfo) {
        this.packageInfo = packageInfo;
    }
}
