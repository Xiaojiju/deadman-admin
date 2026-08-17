package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 商家转账到零钱申请请求（fund-app/mch-transfer）。
 */
public class TransferBillRequest {

    /** 商户 AppId */
    private String appid;
    /** 商户转账单号 */
    private String outBillNo;
    /** 转账场景 ID */
    private String transferSceneId;
    /** 收款用户 openid */
    private String openid;
    /** 转账金额（分） */
    private Long transferAmount;
    /** 转账备注 */
    private String transferRemark;
    /** 收款用户姓名（需与微信实名一致） */
    private String userName;
    /** 转账结果通知 URL */
    private String notifyUrl;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getOutBillNo() {
        return outBillNo;
    }

    public void setOutBillNo(String outBillNo) {
        this.outBillNo = outBillNo;
    }

    public String getTransferSceneId() {
        return transferSceneId;
    }

    public void setTransferSceneId(String transferSceneId) {
        this.transferSceneId = transferSceneId;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Long getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getTransferRemark() {
        return transferRemark;
    }

    public void setTransferRemark(String transferRemark) {
        this.transferRemark = transferRemark;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}
