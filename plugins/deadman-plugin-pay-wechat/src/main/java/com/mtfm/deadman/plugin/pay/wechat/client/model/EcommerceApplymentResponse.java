package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 收付通二级商户进件申请/查询响应。
 */
public class EcommerceApplymentResponse {

    /** 微信支付申请单号 */
    private String applymentId;
    /** 业务申请编号 */
    private String outRequestNo;
    /** 二级商户号（审核通过后返回） */
    private String subMchid;
    /** 申请状态 */
    private String applymentState;
    /** 签约链接 */
    private String signUrl;

    public String getApplymentId() {
        return applymentId;
    }

    public void setApplymentId(String applymentId) {
        this.applymentId = applymentId;
    }

    public String getOutRequestNo() {
        return outRequestNo;
    }

    public void setOutRequestNo(String outRequestNo) {
        this.outRequestNo = outRequestNo;
    }

    public String getSubMchid() {
        return subMchid;
    }

    public void setSubMchid(String subMchid) {
        this.subMchid = subMchid;
    }

    public String getApplymentState() {
        return applymentState;
    }

    public void setApplymentState(String applymentState) {
        this.applymentState = applymentState;
    }

    public String getSignUrl() {
        return signUrl;
    }

    public void setSignUrl(String signUrl) {
        this.signUrl = signUrl;
    }
}
