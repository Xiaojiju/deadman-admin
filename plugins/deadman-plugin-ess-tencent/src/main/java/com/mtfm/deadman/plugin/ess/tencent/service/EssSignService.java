package com.mtfm.deadman.plugin.ess.tencent.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.ess.tencent.client.TencentEssApiGateway;
import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;
import com.mtfm.deadman.plugin.ess.tencent.dto.CreateFlowByFileCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.EssApproverCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.EssSignComponentCommand;
import com.mtfm.deadman.plugin.ess.tencent.util.EssCallbackAes;
import com.mtfm.deadman.plugin.ess.tencent.util.EssCallbackVerifier;
import com.mtfm.deadman.plugin.ess.tencent.util.EssComponentBuilder;
import com.mtfm.deadman.plugin.ess.tencent.vo.CreateFlowByFileResult;
import com.tencentcloudapi.ess.v20201111.models.ApproverInfo;
import com.tencentcloudapi.ess.v20201111.models.CancelFlowResponse;
import com.tencentcloudapi.ess.v20201111.models.Component;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowInfoResponse;

import lombok.RequiredArgsConstructor;

/**
 * 腾讯电子签编排服务：文件发起、查询、撤销与回调解密。
 * <p>
 * 公开回调入口见 {@link com.mtfm.deadman.plugin.ess.tencent.controller.EssTencentCallbackController}，
 * 验签解密后通过 {@link com.mtfm.deadman.plugin.ess.tencent.event.EssFlowCallbackEvent} 通知业务。
 */
@Service
@RequiredArgsConstructor
public class EssSignService {

    private final EssTencentPluginProperties properties;
    private final TencentEssApiGateway tencentEssApiGateway;

    /**
     * 一键通过 PDF 文件发起签署：上传文件 → 创建流程 → 获取签署链接。
     *
     * @param command 发起命令
     * @return 发起结果
     */
    public CreateFlowByFileResult createFlowByFile(CreateFlowByFileCommand command) {
        if (command == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "发起签署请求不能为空");
        }
        properties.requireProductionConfig();
        String operatorUserId = properties.resolveOperatorUserId(command.operatorUserId());
        ApproverInfo[] approvers = toApproverInfos(command.approvers());

        String fileId = tencentEssApiGateway.uploadFile(operatorUserId, command.fileName(), command.fileBytes());
        String flowId = tencentEssApiGateway.createFlowByFiles(
            operatorUserId,
            command.flowName(),
            fileId,
            approvers,
            command.deadlineUnix(),
            command.unordered(),
            command.userData());
        CreateSchemeUrlResponse schemeUrlResponse = tencentEssApiGateway.createSchemeUrl(operatorUserId, flowId);
        return new CreateFlowByFileResult(flowId, schemeUrlResponse.getSchemeUrl(), fileId);
    }

    /**
     * 查询签署流程详情。
     *
     * @param flowId 流程 Id
     * @param operatorUserId 可选经办人
     * @return 流程详情
     */
    public DescribeFlowInfoResponse describeFlowInfo(String flowId, String operatorUserId) {
        properties.requireProductionConfig();
        return tencentEssApiGateway.describeFlowInfo(
            properties.resolveOperatorUserId(operatorUserId), flowId);
    }

    /**
     * 获取合同下载地址。
     *
     * @param flowId 流程 Id
     * @param operatorUserId 可选经办人
     * @return 下载 URL
     */
    public String describeFileUrl(String flowId, String operatorUserId) {
        properties.requireProductionConfig();
        return tencentEssApiGateway.describeFileUrl(
            properties.resolveOperatorUserId(operatorUserId), flowId);
    }

    /**
     * 撤销签署流程。
     *
     * @param flowId 流程 Id
     * @param cancelMessage 撤销原因
     * @param operatorUserId 可选经办人
     * @return 撤销响应
     */
    public CancelFlowResponse cancelFlow(String flowId, String cancelMessage, String operatorUserId) {
        properties.requireProductionConfig();
        return tencentEssApiGateway.cancelFlow(
            properties.resolveOperatorUserId(operatorUserId), flowId, cancelMessage);
    }

    /**
     * 获取签署链接。
     *
     * @param flowId 流程 Id
     * @param operatorUserId 可选经办人
     * @return 签署链接
     */
    public String createSchemeUrl(String flowId, String operatorUserId) {
        properties.requireProductionConfig();
        CreateSchemeUrlResponse response = tencentEssApiGateway.createSchemeUrl(
            properties.resolveOperatorUserId(operatorUserId), flowId);
        return response.getSchemeUrl();
    }

    /**
     * 校验并解密电子签回调报文。
     *
     * @param payload 原始回调报文
     * @param contentSignature 请求头 Content-Signature
     * @return 解密后的明文 JSON；未配置 AES Key 时返回原文
     */
    public String decryptCallbackPayload(String payload, String contentSignature) {
        if (!StringUtils.hasText(properties.getCallbackToken())) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少电子签回调 Token");
        }
        EssCallbackVerifier.requireValid(payload, contentSignature, properties.getCallbackToken());
        if (!StringUtils.hasText(properties.getCallbackAesKey())) {
            return payload;
        }
        return EssCallbackAes.decrypt(payload, properties.getCallbackAesKey());
    }

    private static ApproverInfo[] toApproverInfos(List<EssApproverCommand> commands) {
        if (CollectionUtils.isEmpty(commands)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方不能为空");
        }
        ApproverInfo[] approvers = new ApproverInfo[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            approvers[i] = toApproverInfo(commands.get(i));
        }
        return approvers;
    }

    private static ApproverInfo toApproverInfo(EssApproverCommand command) {
        if (command == null || command.approverType() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署人类型不能为空");
        }
        ApproverInfo approverInfo = new ApproverInfo();
        approverInfo.setApproverType(command.approverType());
        if (StringUtils.hasText(command.approverName())) {
            approverInfo.setApproverName(command.approverName());
        }
        if (StringUtils.hasText(command.approverMobile())) {
            approverInfo.setApproverMobile(command.approverMobile());
        }
        if (StringUtils.hasText(command.organizationName())) {
            approverInfo.setOrganizationName(command.organizationName());
        }
        if (StringUtils.hasText(command.notifyType())) {
            approverInfo.setNotifyType(command.notifyType());
        }
        if (!CollectionUtils.isEmpty(command.signComponents())) {
            Component[] components = command.signComponents().stream()
                .map(EssComponentBuilder::fromCommand)
                .toArray(Component[]::new);
            approverInfo.setSignComponents(components);
        }
        return approverInfo;
    }

    /**
     * 将签署控件命令转换为 SDK 控件（便于业务侧自行组装 ApproverInfo）。
     *
     * @param command 控件命令
     * @return SDK 控件
     */
    public Component buildComponent(EssSignComponentCommand command) {
        return EssComponentBuilder.fromCommand(command);
    }
}
