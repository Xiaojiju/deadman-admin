package com.mtfm.deadman.plugin.ess.tencent.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.ess.tencent.client.TencentEssbasicApiGateway;
import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.CreateFlowsByTemplatesCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelAgentCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelApproverCommand;
import com.mtfm.deadman.plugin.ess.tencent.vo.channel.CreateFlowsByTemplatesResult;
import com.tencentcloudapi.essbasic.v20210526.models.Agent;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCancelFlowResponse;
import com.tencentcloudapi.essbasic.v20210526.models.FlowInfo;

import lombok.RequiredArgsConstructor;

/**
 * 腾讯电子签渠道版（essbasic）编排服务：模板发起、签署链接与撤销。
 * <p>
 * 业务侧应优先使用本服务，企业版 {@link EssSignService} 保留用于历史兼容。
 */
@Service
@RequiredArgsConstructor
public class EssChannelSignService {

    private static final String APPROVER_TYPE_PERSON = "PERSON";

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
