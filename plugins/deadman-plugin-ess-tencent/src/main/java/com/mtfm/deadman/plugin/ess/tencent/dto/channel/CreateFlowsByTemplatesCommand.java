package com.mtfm.deadman.plugin.ess.tencent.dto.channel;

import java.util.List;

/**
 * 渠道版通过模板发起签署流程请求。
 *
 * @param agent 代理发起方（子客企业与经办人 OpenId）
 * @param flowName 合同流程名称
 * @param templateId 模板 Id；可空时回落到配置 {@code deadman.plugin.ess-tencent.template-id}
 * @param formFields 模板表单控件填充列表
 * @param approvers 签署方列表
 * @param customerData 业务透传数据（对应 SDK FlowInfo.customerData，用于回调关联）
 */
public record CreateFlowsByTemplatesCommand(
        EssChannelAgentCommand agent,
        String flowName,
        String templateId,
        List<EssFormFieldCommand> formFields,
        List<EssChannelApproverCommand> approvers,
        String customerData) {
}
