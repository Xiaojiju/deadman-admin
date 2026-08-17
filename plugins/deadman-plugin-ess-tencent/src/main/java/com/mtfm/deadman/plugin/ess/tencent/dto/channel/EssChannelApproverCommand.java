package com.mtfm.deadman.plugin.ess.tencent.dto.channel;

/**
 * 渠道版模板发起签署方信息。
 *
 * @param recipientId 模板签署方 RecipientId（与模板控件绑定）
 * @param approverType 签署方类型：ORGANIZATION（企业）或 PERSON（个人）
 * @param name 签署人姓名
 * @param mobile 签署人手机号
 * @param organizationOpenId 企业 OpenId（企业签署方必填）
 * @param openId 个人 OpenId（个人签署方可选）
 * @param organizationName 企业名称（企业签署方可选）
 * @param approverSignTypes 签署类型数组；含 1 表示静默签（企业章）
 */
public record EssChannelApproverCommand(
        String recipientId,
        String approverType,
        String name,
        String mobile,
        String organizationOpenId,
        String openId,
        String organizationName,
        Long[] approverSignTypes) {
}
