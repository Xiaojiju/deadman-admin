package com.mtfm.deadman.plugin.ess.tencent.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.ess.tencent.client.TencentEssbasicApiGateway;
import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.ChannelCreateFlowByFilesCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.CreateFlowsByTemplatesCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelAgentCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelApproverCommand;
import com.mtfm.deadman.plugin.ess.tencent.vo.channel.ChannelCreateFlowByFilesResult;
import com.mtfm.deadman.plugin.ess.tencent.vo.channel.ChannelOrganizationStatusVO;
import com.mtfm.deadman.plugin.ess.tencent.vo.channel.CreateConsoleLoginUrlResult;
import com.mtfm.deadman.plugin.ess.tencent.vo.channel.CreateFlowsByTemplatesResult;
import com.tencentcloudapi.essbasic.v20210526.models.Agent;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCancelFlowResponse;
import com.tencentcloudapi.essbasic.v20210526.models.FlowInfo;

import lombok.RequiredArgsConstructor;

/**
 * 腾讯电子签渠道版（essbasic）编排服务：子客认证、文件发起签署、签署链接与撤销。
 * <p>
 * 业务侧应优先使用本服务，企业版 {@link EssSignService} 保留用于历史兼容。
 */
@Service
@RequiredArgsConstructor
public class EssChannelSignService {

    private static final String APPROVER_TYPE_PERSON = "PERSON";
    private static final String APPROVER_TYPE_ORGANIZATION = "ORGANIZATION";

    private final EssTencentPluginProperties properties;
    private final TencentEssbasicApiGateway tencentEssbasicApiGateway;

    /**
     * 通过模板发起签署：创建流程 → 获取签署链接。
     *
     * @param command 模板发起命令
     * @return 发起结果（含 flowId 与签署路径）
     */
    public CreateFlowsByTemplatesResult createFlowByTemplate(CreateFlowsByTemplatesCommand command) {
        if (command == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "模板发起请求不能为空");
        }
        properties.requireChannelConfig();
        Agent agent = tencentEssbasicApiGateway.buildAgent(properties.getAppId(), command.agent());
        String templateId = properties.resolveTemplateId(command.templateId());
        FlowInfo flowInfo = tencentEssbasicApiGateway.buildFlowInfo(
            command.flowName(),
            templateId,
            command.formFields(),
            command.approvers(),
            command.customerData());

        String flowId = tencentEssbasicApiGateway.createFlowsByTemplates(agent, flowInfo);
        EssChannelApproverCommand signUrlApprover = resolveSignUrlApprover(command.approvers());
        String schemeUrl = tencentEssbasicApiGateway.createSignUrls(
            agent,
            flowId,
            signUrlApprover.name(),
            signUrlApprover.mobile(),
            true,
            "APP");
        return new CreateFlowsByTemplatesResult(flowId, schemeUrl, null);
    }

    /**
     * 创建子客控制台登录/企业认证链接（默认 {@code endpoint=APP}，便于己方小程序跳转）。
     *
     * @param agent 子客 OpenId
     * @param organizationName 企业名称
     * @param creditCode 统一社会信用代码，可空
     * @param operatorName 经办人姓名
     * @param operatorMobile 经办人手机
     * @return 认证链接与激活状态
     */
    public CreateConsoleLoginUrlResult createConsoleLoginUrl(
            EssChannelAgentCommand agent,
            String organizationName,
            String creditCode,
            String operatorName,
            String operatorMobile) {
        return createConsoleLoginUrl(agent, organizationName, creditCode, operatorName, operatorMobile, "APP");
    }

    /**
     * 创建子客控制台登录/企业认证链接。
     *
     * @param agent 子客 OpenId
     * @param organizationName 企业名称
     * @param creditCode 统一社会信用代码，可空
     * @param operatorName 经办人姓名
     * @param operatorMobile 经办人手机
     * @param endpoint 跳转端类型；为空时默认 {@code APP}。可选值见
     *                 {@link TencentEssbasicApiGateway#createConsoleLoginUrl}：
     *                 PC / CHANNEL / SHORT_URL / WEIXIN_QRCODE_URL / APP / H5 / SHORT_H5
     * @return 认证链接与激活状态
     */
    public CreateConsoleLoginUrlResult createConsoleLoginUrl(
            EssChannelAgentCommand agent,
            String organizationName,
            String creditCode,
            String operatorName,
            String operatorMobile,
            String endpoint) {
        properties.requireChannelConfig();
        String resolvedEndpoint = StringUtils.hasText(endpoint) ? endpoint.trim() : "APP";
        return tencentEssbasicApiGateway.createConsoleLoginUrl(
            agent, organizationName, creditCode, operatorName, operatorMobile, resolvedEndpoint);
    }

    /**
     * 查询子客企业激活状态。
     *
     * @param organizationOpenId 子客企业 OpenId
     * @return 状态，未找到为 null
     */
    public ChannelOrganizationStatusVO describeChannelOrganization(String organizationOpenId) {
        properties.requireChannelConfig();
        return tencentEssbasicApiGateway.describeChannelOrganization(organizationOpenId);
    }

    /**
     * 查询子客默认企业印章 Id。
     *
     * @param agent 子客代理
     * @return 印章 Id，无可用印章时为 null
     */
    public String describeDefaultSealId(EssChannelAgentCommand agent) {
        properties.requireChannelConfig();
        return tencentEssbasicApiGateway.describeDefaultSealId(agent);
    }

    /**
     * 上传定稿 PDF 并通过文件发起有序签署，分别生成商户与买家签署链接。
     *
     * @param command 文件发起命令
     * @param fileName 上传文件名
     * @param fileBytes 定稿 PDF 字节
     * @return flowId 与双方签署路径
     */
    public ChannelCreateFlowByFilesResult createFlowByFile(
            ChannelCreateFlowByFilesCommand command, String fileName, byte[] fileBytes) {
        if (command == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件发起请求不能为空");
        }
        properties.requireChannelConfig();
        String fileId = tencentEssbasicApiGateway.uploadFile(command.agent(), fileName, fileBytes);
        ChannelCreateFlowByFilesCommand withFile = new ChannelCreateFlowByFilesCommand(
            command.agent(),
            command.flowName(),
            fileId,
            command.approvers(),
            command.customerData(),
            command.deadlineUnix(),
            command.unordered());
        String flowId = tencentEssbasicApiGateway.channelCreateFlowByFiles(withFile);
        Agent sdkAgent = tencentEssbasicApiGateway.buildAgent(properties.getAppId(), command.agent());
        EssChannelApproverCommand merchant = resolveApproverByType(command.approvers(), APPROVER_TYPE_ORGANIZATION);
        EssChannelApproverCommand buyer = resolveApproverByType(command.approvers(), APPROVER_TYPE_PERSON);
        String merchantSchemeUrl = tencentEssbasicApiGateway.createSignUrls(
            sdkAgent, flowId, merchant.name(), merchant.mobile(), true, "APP");
        String buyerSchemeUrl = tencentEssbasicApiGateway.createSignUrls(
            sdkAgent, flowId, buyer.name(), buyer.mobile(), true, "APP");
        return new ChannelCreateFlowByFilesResult(flowId, buyerSchemeUrl, merchantSchemeUrl);
    }

    /**
     * 撤销渠道版签署流程。
     *
     * @param agent 代理发起方
     * @param flowId 流程 Id
     * @param reason 撤销原因
     * @return 撤销响应
     */
    public ChannelCancelFlowResponse cancelChannelFlow(EssChannelAgentCommand agent, String flowId, String reason) {
        properties.requireChannelConfig();
        Agent sdkAgent = tencentEssbasicApiGateway.buildAgent(properties.getAppId(), agent);
        return tencentEssbasicApiGateway.channelCancelFlow(sdkAgent, flowId, reason);
    }

    private static EssChannelApproverCommand resolveApproverByType(
            List<EssChannelApproverCommand> approvers, String approverType) {
        if (CollectionUtils.isEmpty(approvers)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方不能为空");
        }
        for (EssChannelApproverCommand approver : approvers) {
            if (approver != null
                    && approverType.equalsIgnoreCase(approver.approverType())
                    && StringUtils.hasText(approver.name())
                    && StringUtils.hasText(approver.mobile())) {
                return approver;
            }
        }
        return resolveSignUrlApprover(approvers);
    }

    /**
     * 选取用于 CreateSignUrls 的个人签署方：优先第一个 PERSON，否则取第一个有姓名和手机号的签署方。
     *
     * @param approvers 签署方列表
     * @return 用于生成签署链接的签署方
     */
    private static EssChannelApproverCommand resolveSignUrlApprover(List<EssChannelApproverCommand> approvers) {
        if (CollectionUtils.isEmpty(approvers)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方不能为空");
        }
        for (EssChannelApproverCommand approver : approvers) {
            if (approver != null
                    && APPROVER_TYPE_PERSON.equalsIgnoreCase(approver.approverType())
                    && StringUtils.hasText(approver.name())
                    && StringUtils.hasText(approver.mobile())) {
                return approver;
            }
        }
        for (EssChannelApproverCommand approver : approvers) {
            if (approver != null
                    && StringUtils.hasText(approver.name())
                    && StringUtils.hasText(approver.mobile())) {
                return approver;
            }
        }
        throw new BusinessException(ResultCode.BAD_REQUEST, "未找到可用于生成签署链接的签署人（需姓名与手机号）");
    }
}
