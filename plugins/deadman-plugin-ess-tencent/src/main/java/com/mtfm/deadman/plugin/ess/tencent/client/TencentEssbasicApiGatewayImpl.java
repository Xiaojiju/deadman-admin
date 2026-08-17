package com.mtfm.deadman.plugin.ess.tencent.client;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelAgentCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelApproverCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssFormFieldCommand;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.essbasic.v20210526.EssbasicClient;
import com.tencentcloudapi.essbasic.v20210526.models.Agent;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCancelFlowRequest;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCancelFlowResponse;
import com.tencentcloudapi.essbasic.v20210526.models.CreateFlowsByTemplatesRequest;
import com.tencentcloudapi.essbasic.v20210526.models.CreateFlowsByTemplatesResponse;
import com.tencentcloudapi.essbasic.v20210526.models.CreateSignUrlsRequest;
import com.tencentcloudapi.essbasic.v20210526.models.CreateSignUrlsResponse;
import com.tencentcloudapi.essbasic.v20210526.models.FlowApproverInfo;
import com.tencentcloudapi.essbasic.v20210526.models.FlowInfo;
import com.tencentcloudapi.essbasic.v20210526.models.FormField;
import com.tencentcloudapi.essbasic.v20210526.models.SignUrlInfo;
import com.tencentcloudapi.essbasic.v20210526.models.UserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯电子签渠道版（essbasic）API 网关实现。
 */
@Slf4j
@RequiredArgsConstructor
public class TencentEssbasicApiGatewayImpl implements TencentEssbasicApiGateway {

    private static final String APPROVER_TYPE_ORGANIZATION = "ORGANIZATION";
    private static final long SILENT_SIGN_TYPE = 1L;

    private final EssbasicClientFactory essbasicClientFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public String createFlowsByTemplates(Agent agent, FlowInfo flowInfo) {
        if (agent == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "渠道代理信息不能为空");
        }
        if (flowInfo == null || !StringUtils.hasText(flowInfo.getFlowName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "流程名称不能为空");
        }
        if (!StringUtils.hasText(flowInfo.getTemplateId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "模板 Id 不能为空");
        }
        if (flowInfo.getFlowApprovers() == null || flowInfo.getFlowApprovers().length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方不能为空");
        }
        try {
            EssbasicClient client = essbasicClientFactory.createApiClient();
            CreateFlowsByTemplatesRequest request = new CreateFlowsByTemplatesRequest();
            request.setAgent(agent);
            request.setFlowInfos(new FlowInfo[] { flowInfo });

            CreateFlowsByTemplatesResponse response = client.CreateFlowsByTemplates(request);
            if (response.getErrorMessages() != null && response.getErrorMessages().length > 0
                    && StringUtils.hasText(response.getErrorMessages()[0])) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "创建签署流程失败: " + response.getErrorMessages()[0]);
            }
            if (response.getFlowIds() == null || response.getFlowIds().length == 0
                    || !StringUtils.hasText(response.getFlowIds()[0])) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "创建签署流程成功但未返回 flowId");
            }
            return response.getFlowIds()[0];
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("渠道版电子签创建流程失败, flowName={}, templateId={}, code={}, message={}",
                    flowInfo.getFlowName(), flowInfo.getTemplateId(), ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "创建签署流程失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSignUrls(
            Agent agent,
            String flowId,
            String name,
            String mobile,
            boolean autoJumpBack,
            String endpoint) {
        requireFlowId(flowId);
        if (!StringUtils.hasText(name) || !StringUtils.hasText(mobile)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署人姓名与手机号不能为空");
        }
        try {
            EssbasicClient client = essbasicClientFactory.createApiClient();
            CreateSignUrlsRequest request = new CreateSignUrlsRequest();
            request.setAgent(agent);
            request.setFlowIds(new String[] { flowId });
            request.setName(name);
            request.setMobile(mobile);
            request.setAutoJumpBack(autoJumpBack);
            request.setEndpoint(StringUtils.hasText(endpoint) ? endpoint : "APP");

            CreateSignUrlsResponse response = client.CreateSignUrls(request);
            if (response.getErrorMessages() != null && response.getErrorMessages().length > 0
                    && StringUtils.hasText(response.getErrorMessages()[0])) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "获取签署链接失败: " + response.getErrorMessages()[0]);
            }
            SignUrlInfo[] signUrlInfos = response.getSignUrlInfos();
            if (signUrlInfos == null || signUrlInfos.length == 0) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "获取签署链接成功但未返回 SignUrlInfo");
            }
            String signPath = resolveSignPath(signUrlInfos[0]);
            if (!StringUtils.hasText(signPath)) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "未获取到有效签署链接");
            }
            return signPath;
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("渠道版电子签获取签署链接失败, flowId={}, code={}, message={}",
                    flowId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "获取签署链接失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelCancelFlowResponse channelCancelFlow(Agent agent, String flowId, String cancelMessage) {
        requireFlowId(flowId);
        try {
            EssbasicClient client = essbasicClientFactory.createApiClient();
            ChannelCancelFlowRequest request = new ChannelCancelFlowRequest();
            request.setAgent(agent);
            request.setFlowId(flowId);
            request.setCancelMessage(StringUtils.hasText(cancelMessage) ? cancelMessage : "业务撤销");
            return client.ChannelCancelFlow(request);
        } catch (TencentCloudSDKException ex) {
            log.error("渠道版电子签撤销流程失败, flowId={}, code={}, message={}",
                    flowId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "撤销签署流程失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Agent buildAgent(String appId, EssChannelAgentCommand agentCommand) {
        if (!StringUtils.hasText(appId)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签渠道 AppId");
        }
        if (agentCommand == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "渠道代理信息不能为空");
        }
        if (!StringUtils.hasText(agentCommand.proxyOrganizationOpenId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "代理企业 OpenId 不能为空");
        }
        if (!StringUtils.hasText(agentCommand.proxyOperatorOpenId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "代理经办人 OpenId 不能为空");
        }
        Agent agent = new Agent();
        agent.setAppId(appId);
        agent.setProxyOrganizationOpenId(agentCommand.proxyOrganizationOpenId());
        UserInfo proxyOperator = new UserInfo();
        proxyOperator.setOpenId(agentCommand.proxyOperatorOpenId());
        agent.setProxyOperator(proxyOperator);
        return agent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowInfo buildFlowInfo(
            String flowName,
            String templateId,
            java.util.List<EssFormFieldCommand> formFields,
            java.util.List<EssChannelApproverCommand> approvers,
            String customerData) {
        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setFlowName(flowName);
        flowInfo.setTemplateId(templateId);
        flowInfo.setFlowApprovers(toFlowApproverInfos(approvers));
        if (formFields != null && !formFields.isEmpty()) {
            flowInfo.setFormFields(toFormFields(formFields));
        }
        if (StringUtils.hasText(customerData)) {
            flowInfo.setCustomerData(customerData);
        }
        return flowInfo;
    }

    private static FlowApproverInfo[] toFlowApproverInfos(java.util.List<EssChannelApproverCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方不能为空");
        }
        FlowApproverInfo[] approvers = new FlowApproverInfo[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            approvers[i] = toFlowApproverInfo(commands.get(i));
        }
        return approvers;
    }

    private static FlowApproverInfo toFlowApproverInfo(EssChannelApproverCommand command) {
        if (command == null || !StringUtils.hasText(command.approverType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方类型不能为空");
        }
        FlowApproverInfo approverInfo = new FlowApproverInfo();
        approverInfo.setApproverType(command.approverType());
        if (StringUtils.hasText(command.recipientId())) {
            approverInfo.setRecipientId(command.recipientId());
        }
        if (StringUtils.hasText(command.name())) {
            approverInfo.setName(command.name());
        }
        if (StringUtils.hasText(command.mobile())) {
            approverInfo.setMobile(command.mobile());
        }
        if (StringUtils.hasText(command.organizationName())) {
            approverInfo.setOrganizationName(command.organizationName());
        }
        if (StringUtils.hasText(command.organizationOpenId())) {
            approverInfo.setOrganizationOpenId(command.organizationOpenId());
        }
        if (StringUtils.hasText(command.openId())) {
            approverInfo.setOpenId(command.openId());
        }
        if (APPROVER_TYPE_ORGANIZATION.equalsIgnoreCase(command.approverType())
                && command.approverSignTypes() != null
                && Arrays.asList(command.approverSignTypes()).contains(SILENT_SIGN_TYPE)) {
            approverInfo.setApproverSignTypes(command.approverSignTypes());
        }
        return approverInfo;
    }

    private static FormField[] toFormFields(java.util.List<EssFormFieldCommand> commands) {
        FormField[] formFields = new FormField[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            EssFormFieldCommand command = commands.get(i);
            FormField formField = new FormField();
            if (StringUtils.hasText(command.componentName())) {
                formField.setComponentName(command.componentName());
            }
            if (StringUtils.hasText(command.componentValue())) {
                formField.setComponentValue(command.componentValue());
            }
            formFields[i] = formField;
        }
        return formFields;
    }

    /**
     * 从 SignUrlInfo 解析签署路径：优先 SchemeUrl，其次 SignUrl / HttpSignUrl / SignQrcodeUrl。
     *
     * @param signUrlInfo SDK 签署链接信息
     * @return 签署路径
     */
    static String resolveSignPath(SignUrlInfo signUrlInfo) {
        if (signUrlInfo == null) {
            return null;
        }
        String schemeUrl = invokeStringGetter(signUrlInfo, "getSchemeUrl");
        if (StringUtils.hasText(schemeUrl)) {
            return schemeUrl;
        }
        if (StringUtils.hasText(signUrlInfo.getSignUrl())) {
            return signUrlInfo.getSignUrl();
        }
        String httpSignUrl = invokeStringGetter(signUrlInfo, "getHttpSignUrl");
        if (StringUtils.hasText(httpSignUrl)) {
            return httpSignUrl;
        }
        if (StringUtils.hasText(signUrlInfo.getSignQrcodeUrl())) {
            return signUrlInfo.getSignQrcodeUrl();
        }
        return null;
    }

    private static String invokeStringGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof String str && StringUtils.hasText(str) ? str : null;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static void requireFlowId(String flowId) {
        if (!StringUtils.hasText(flowId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "flowId 不能为空");
        }
    }
}
