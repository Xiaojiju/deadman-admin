package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 国内普通退款异常退款申请请求体。
 */
public class DomesticAbnormalRefundRequest {

    /** 商户退款单号 */
    private String outRefundNo;
    /** 退款入账方类型：USER_BANK_CARD / MERCHANT_BANK_CARD */
    private String type;
    /** 银行类型（USER_BANK_CARD 时必填） */
    private String bankType;
    /** 银行卡号（USER_BANK_CARD 时必填） */
    private String bankAccount;
    /** 真实姓名（USER_BANK_CARD 时必填） */
    private String realName;

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
