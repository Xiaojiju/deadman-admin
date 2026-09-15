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
     * 创建子客控制台登录/认证链接（首次调用即预创建空壳子客）。
     *
     * @param agent 代理发起方（须带自定义子客 OpenId）
     * @param organizationName 子客企业名称
     * @param creditCode 统一社会信用代码，可空
     * @param operatorName 经办人姓名
     * @param operatorMobile 经办人手机号
     * @param endpoint 跳转端类型，可选值：
     *                 <ul>
     *                   <li>{@code PC}：（默认）web 控制台链接，需在 PC 浏览器中打开</li>
     *                   <li>{@code CHANNEL}：H5 跳转电子签小程序链接，一般用于短信中的链接</li>
     *                   <li>{@code SHORT_URL}：H5 跳转电子签小程序的短链，一般用于短信中的链接</li>
     *                   <li>{@code WEIXIN_QRCODE_URL}：直接跳转电子签小程序的二维码链接（需自行转二维码后微信扫码；直接点链接无效）</li>
     *                   <li>{@code APP}：APP/小程序跳转电子签小程序链接，一般用于己方小程序或 APP 跳转</li>
     *                   <li>{@code H5}：H5 长链接跳转电子签 H5 页面</li>
     *                   <li>{@code SHORT_H5}：H5 短链跳转电子签 H5 页面，一般用于短信中的链接</li>
     *                 </ul>
     * @return 控制台链接与激活状态
     */
    com.mtfm.deadman.plugin.ess.tencent.vo.channel.CreateConsoleLoginUrlResult createConsoleLoginUrl(
            EssChannelAgentCommand agent,
            String organizationName,
            String creditCode,
            String operatorName,
            String operatorMobile,
            String endpoint);

    /**
     * 查询单个子客企业激活与许可状态。
     *
     * @param organizationOpenId 子客企业 OpenId
     * @return 子客状态，未找到时为 null
     */
    com.mtfm.deadman.plugin.ess.tencent.vo.channel.ChannelOrganizationStatusVO describeChannelOrganization(
            String organizationOpenId);

    /**
     * 查询子客企业印章列表，优先返回可用的企业主印章 Id。
     *
     * @param agent 代理发起方
     * @return 印章 Id，无可用印章时为 null
     */
    String describeDefaultSealId(EssChannelAgentCommand agent);

    /**
     * 上传合同文件到渠道版文件服务，返回 FileId。
     *
     * @param agent 代理发起方
     * @param fileName 文件名
     * @param fileBytes 文件字节
     * @return 腾讯文件 Id
     */
    String uploadFile(EssChannelAgentCommand agent, String fileName, byte[] fileBytes);

    /**
     * 通过文件发起签署流程（ChannelCreateFlowByFiles）。
     *
     * @param command 文件发起命令
     * @return flowId
     */
    String channelCreateFlowByFiles(
            com.mtfm.deadman.plugin.ess.tencent.dto.channel.ChannelCreateFlowByFilesCommand command);

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
