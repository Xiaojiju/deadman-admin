package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 合单子单（下单请求）。
 * <p>
 * 合作伙伴/平台收付通：{@code mchid} 为合单发起方商户号，{@code subMchid} 为二级商户号。
 */
public class CombineSubOrder {

    /** 商品单发起方商户号（合单平台/服务商商户号） */
    private String mchid;
    /** 二级商户号（特约商户） */
    private String subMchid;
    /** 子单商户订单号 */
    private String outTradeNo;
    /** 商品描述 */
    private String description;
    /** 附加数据（渠道必填，无业务值时可传空串） */
    private String attach;
    /** 金额 */
    private CombineAmount amount;
    /** 结算信息 */
    private CombineSettleInfo settleInfo;

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String mchid) {
        this.mchid = mchid;
    }

    public String getSubMchid() {
        return subMchid;
    }

    public void setSubMchid(String subMchid) {
        this.subMchid = subMchid;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public CombineAmount getAmount() {
        return amount;
    }

    public void setAmount(CombineAmount amount) {
        this.amount = amount;
    }

    public CombineSettleInfo getSettleInfo() {
        return settleInfo;
    }

    public void setSettleInfo(CombineSettleInfo settleInfo) {
        this.settleInfo = settleInfo;
    }
}
