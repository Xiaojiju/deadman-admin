package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通二级商户进件结果。
 *
 * @param applymentId    微信支付申请单号
 * @param outRequestNo   业务申请编号
 * @param subMchid       二级商户号（审核通过后返回）
 * @param applymentState 申请状态
 * @param signUrl        签约链接（需签约时返回）
 */
public record WechatEcommerceApplymentResult(
        String applymentId, String outRequestNo, String subMchid, String applymentState, String signUrl) {
}
