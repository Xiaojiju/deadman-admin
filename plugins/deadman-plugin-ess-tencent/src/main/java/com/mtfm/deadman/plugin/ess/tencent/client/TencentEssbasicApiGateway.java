package com.mtfm.deadman.plugin.ess.tencent.client;

import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelAgentCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelApproverCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssFormFieldCommand;
import com.tencentcloudapi.essbasic.v20210526.models.Agent;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCancelFlowResponse;
import com.tencentcloudapi.essbasic.v20210526.models.FlowInfo;

/**
 * 腾讯电子签渠道版（essbasic）API 网关。
 */
public interface TencentEssbasicApiGateway {

    /**
     * 通过模板批量创建签署流程，返回首个 flowId。
     *
     * @param agent 渠道代理信息
     * @param flowInfo 流程信息（含模板、签署方、表单控件）
     * @return flowId
     */
    String createFlowsByTemplates(Agent agent, FlowInfo flowInfo);

    /**
     * 创建签署链接（小程序/APP 路径）。
     *
     * @param agent 渠道代理信息
     * @param flowId 流程 Id
     * @param name 签署人姓名
     * @param mobile 签署人手机号
     * @param autoJumpBack 签署完成后是否自动跳回
     * @param endpoint 签署端类型，如 APP
     * @return 签署链接或小程序 scheme 路径
     */
    String createSignUrls(
            Agent agent,
            String flowId,
            String name,
            String mobile,
            boolean autoJumpBack,
            String endpoint);

    /**
     * 撤销渠道版签署流程。
     *
     * @param agent 渠道代理信息
     * @param flowId 流程 Id
     * @param cancelMessage 撤销原因
     * @return 撤销响应
     */
    ChannelCancelFlowResponse channelCancelFlow(Agent agent, String flowId, String cancelMessage);

    /**
     * 根据配置 AppId 与代理命令构造 SDK Agent。
     *
     * @param appId 渠道 AppId
     * @param agentCommand 代理企业与经办人 OpenId
     * @return SDK Agent
     */
    Agent buildAgent(String appId, EssChannelAgentCommand agentCommand);

    /**
     * 将业务命令转换为 SDK FlowInfo。
     *
     * @param flowName 流程名称
     * @param templateId 模板 Id
     * @param formFields 表单控件
     * @param approvers 签署方
     * @param customerData 业务透传数据
     * @return SDK FlowInfo
     */
    FlowInfo buildFlowInfo(
            String flowName,
            String templateId,
            java.util.List<EssFormFieldCommand> formFields,
            java.util.List<EssChannelApproverCommand> approvers,
            String customerData);
}
