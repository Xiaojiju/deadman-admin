package com.mtfm.deadman.plugin.pay.wechat.client.model;

import java.util.List;

/**
 * 合单下单-JSAPI 请求体（SDK 暂无专用 Service，配合 HttpClient + GsonUtil）。
 */
public class CombineJsapiPrepayRequest {

    /** 合单商户 AppId */
    private String combineAppid;
    /** 合单商户号 */
    private String combineMchid;
    /** 合单商户订单号 */
    private String combineOutTradeNo;
    /** 合单支付者信息 */
    private CombinePayerInfo combinePayerInfo;
    /** 子单列表 */
    private List<CombineSubOrder> subOrders;
    /** 支付结果通知 URL */
    private String notifyUrl;
    /** 场景信息 */
    private CombineSceneInfo sceneInfo;

    public String getCombineAppid() {
        return combineAppid;
    }

    public void setCombineAppid(String combineAppid) {
        this.combineAppid = combineAppid;
    }

    public String getCombineMchid() {
        return combineMchid;
    }

    public void setCombineMchid(String combineMchid) {
        this.combineMchid = combineMchid;
    }

    public String getCombineOutTradeNo() {
        return combineOutTradeNo;
    }

    public void setCombineOutTradeNo(String combineOutTradeNo) {
        this.combineOutTradeNo = combineOutTradeNo;
    }

    public CombinePayerInfo getCombinePayerInfo() {
        return combinePayerInfo;
    }

    public void setCombinePayerInfo(CombinePayerInfo combinePayerInfo) {
        this.combinePayerInfo = combinePayerInfo;
    }

    public List<CombineSubOrder> getSubOrders() {
        return subOrders;
    }

    public void setSubOrders(List<CombineSubOrder> subOrders) {
        this.subOrders = subOrders;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public CombineSceneInfo getSceneInfo() {
        return sceneInfo;
    }

    public void setSceneInfo(CombineSceneInfo sceneInfo) {
        this.sceneInfo = sceneInfo;
    }
}
