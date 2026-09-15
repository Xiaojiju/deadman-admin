package com.mtfm.deadman.plugin.ess.tencent.client;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;
import com.mtfm.deadman.plugin.ess.tencent.dto.EssSignComponentCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.ChannelCreateFlowByFilesCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelAgentCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssChannelApproverCommand;
import com.mtfm.deadman.plugin.ess.tencent.dto.channel.EssFormFieldCommand;
import com.mtfm.deadman.plugin.ess.tencent.vo.channel.ChannelOrganizationStatusVO;
import com.mtfm.deadman.plugin.ess.tencent.vo.channel.CreateConsoleLoginUrlResult;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.essbasic.v20210526.EssbasicClient;
import com.tencentcloudapi.essbasic.v20210526.models.Agent;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCancelFlowRequest;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCancelFlowResponse;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCreateFlowByFilesRequest;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelCreateFlowByFilesResponse;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelDescribeOrganizationSealsRequest;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelDescribeOrganizationSealsResponse;
import com.tencentcloudapi.essbasic.v20210526.models.ChannelOrganizationInfo;
import com.tencentcloudapi.essbasic.v20210526.models.Component;
import com.tencentcloudapi.essbasic.v20210526.models.CreateConsoleLoginUrlRequest;
import com.tencentcloudapi.essbasic.v20210526.models.CreateConsoleLoginUrlResponse;
import com.tencentcloudapi.essbasic.v20210526.models.CreateFlowsByTemplatesRequest;
import com.tencentcloudapi.essbasic.v20210526.models.CreateFlowsByTemplatesResponse;
import com.tencentcloudapi.essbasic.v20210526.models.CreateSignUrlsRequest;
import com.tencentcloudapi.essbasic.v20210526.models.CreateSignUrlsResponse;
import com.tencentcloudapi.essbasic.v20210526.models.DescribeChannelOrganizationsRequest;
import com.tencentcloudapi.essbasic.v20210526.models.DescribeChannelOrganizationsResponse;
import com.tencentcloudapi.essbasic.v20210526.models.FlowApproverInfo;
import com.tencentcloudapi.essbasic.v20210526.models.FlowInfo;
import com.tencentcloudapi.essbasic.v20210526.models.FormField;
import com.tencentcloudapi.essbasic.v20210526.models.OccupiedSeal;
import com.tencentcloudapi.essbasic.v20210526.models.SignUrlInfo;
import com.tencentcloudapi.essbasic.v20210526.models.UploadFile;
import com.tencentcloudapi.essbasic.v20210526.models.UploadFilesRequest;
import com.tencentcloudapi.essbasic.v20210526.models.UploadFilesResponse;
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
    private static final String SEAL_STATUS_SUCCESS = "SUCCESS";

    private final EssbasicClientFactory essbasicClientFactory;
    private final EssTencentPluginProperties properties;

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
    public CreateConsoleLoginUrlResult createConsoleLoginUrl(
            EssChannelAgentCommand agent,
            String organizationName,
            String creditCode,
            String operatorName,
            String operatorMobile,
            String endpoint) {
        if (!StringUtils.hasText(organizationName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "子客企业名称不能为空");
        }
        if (!StringUtils.hasText(operatorName) || !StringUtils.hasText(operatorMobile)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "经办人姓名与手机号不能为空");
        }
        try {
            EssbasicClient client = essbasicClientFactory.createApiClient();
            CreateConsoleLoginUrlRequest request = new CreateConsoleLoginUrlRequest();
            request.setAgent(buildAgent(properties.getAppId(), agent));
            request.setProxyOrganizationName(organizationName.trim());
            if (StringUtils.hasText(creditCode)) {
                request.setUniformSocialCreditCode(creditCode.trim());
            }
            request.setProxyOperatorName(operatorName.trim());
            request.setProxyOperatorMobile(operatorMobile.trim());
            request.setEndpoint(StringUtils.hasText(endpoint) ? endpoint : "APP");
            CreateConsoleLoginUrlResponse response = client.CreateConsoleLoginUrl(request);
            return new CreateConsoleLoginUrlResult(
                response.getConsoleUrl(),
                Boolean.TRUE.equals(response.getIsActivated()),
                Boolean.TRUE.equals(response.getProxyOperatorIsVerified()));
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("创建电子签子客认证链接失败, org={}, code={}, message={}",
                    organizationName, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "创建电子签认证链接失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelOrganizationStatusVO describeChannelOrganization(String organizationOpenId) {
        if (!StringUtils.hasText(organizationOpenId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "子客企业 OpenId 不能为空");
        }
        try {
            EssbasicClient client = essbasicClientFactory.createApiClient();
            DescribeChannelOrganizationsRequest request = new DescribeChannelOrganizationsRequest();
            Agent agent = new Agent();
            agent.setAppId(properties.getAppId());
            request.setAgent(agent);
            request.setOrganizationOpenId(organizationOpenId.trim());
            request.setLimit(1L);
            DescribeChannelOrganizationsResponse response = client.DescribeChannelOrganizations(request);
            ChannelOrganizationInfo[] infos = response.getChannelOrganizationInfos();
            if (infos == null || infos.length == 0) {
                return null;
            }
            ChannelOrganizationInfo info = infos[0];
            return new ChannelOrganizationStatusVO(
                info.getOrganizationOpenId(),
                info.getOrganizationName(),
                info.getActiveStatus(),
                info.getLicenseExpireTime(),
                info.getAuthorizationStatus());
        } catch (TencentCloudSDKException ex) {
            log.error("查询电子签子客状态失败, orgOpenId={}, code={}, message={}",
                    organizationOpenId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "查询电子签企业状态失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describeDefaultSealId(EssChannelAgentCommand agent) {
        try {
            EssbasicClient client = essbasicClientFactory.createApiClient();
            ChannelDescribeOrganizationSealsRequest request = new ChannelDescribeOrganizationSealsRequest();
            request.setAgent(buildAgent(properties.getAppId(), agent));
            request.setLimit(20L);
            request.setOffset(0L);
            ChannelDescribeOrganizationSealsResponse response = client.ChannelDescribeOrganizationSeals(request);
            OccupiedSeal[] seals = response.getSeals();
            if (seals == null || seals.length == 0) {
                return null;
            }
            for (OccupiedSeal seal : seals) {
                if (seal == null || !StringUtils.hasText(seal.getSealId())) {
                    continue;
                }
                String status = seal.getSealStatus();
                if (!StringUtils.hasText(status) || SEAL_STATUS_SUCCESS.equalsIgnoreCase(status)) {
                    return seal.getSealId();
                }
            }
            return seals[0].getSealId();
        } catch (TencentCloudSDKException ex) {
            log.warn("查询电子签企业印章失败, orgOpenId={}, message={}",
                    agent == null ? null : agent.proxyOrganizationOpenId(), ex.getMessage());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uploadFile(EssChannelAgentCommand agent, String fileName, byte[] fileBytes) {
        if (!StringUtils.hasText(fileName) || fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "电子签上传文件内容不能为空");
        }
        try {
            EssbasicClient client = essbasicClientFactory.createFileClient();
            UploadFilesRequest request = new UploadFilesRequest();
            request.setAgent(buildAgent(properties.getAppId(), agent));
            request.setBusinessType("DOCUMENT");
            UploadFile uploadFile = new UploadFile();
            uploadFile.setFileName(fileName);
            uploadFile.setFileBody(Base64.getEncoder().encodeToString(fileBytes));
            request.setFileInfos(new UploadFile[] { uploadFile });
            UploadFilesResponse response = client.UploadFiles(request);
            if (response.getFileIds() == null || response.getFileIds().length == 0
                    || !StringUtils.hasText(response.getFileIds()[0])) {
                throw new BusinessException(ResultCode.ESS_UPLOAD_FAILED, "电子签上传成功但未返回 fileId");
            }
            return response.getFileIds()[0];
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("渠道版电子签上传文件失败, fileName={}, code={}, message={}",
                    fileName, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_UPLOAD_FAILED, "电子签文件上传失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String channelCreateFlowByFiles(ChannelCreateFlowByFilesCommand command) {
        if (command == null || !StringUtils.hasText(command.flowName()) || !StringUtils.hasText(command.fileId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "流程名称与文件 Id 不能为空");
        }
        if (CollectionUtils.isEmpty(command.approvers())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方不能为空");
        }
        try {
            EssbasicClient client = essbasicClientFactory.createApiClient();
            ChannelCreateFlowByFilesRequest request = new ChannelCreateFlowByFilesRequest();
            request.setAgent(buildAgent(properties.getAppId(), command.agent()));
            request.setFlowName(command.flowName());
            request.setFileIds(new String[] { command.fileId() });
            request.setFlowApprovers(toFlowApproverInfos(command.approvers()));
            request.setUnordered(command.unordered());
            if (command.deadlineUnix() != null) {
                request.setDeadline(command.deadlineUnix());
            }
            if (StringUtils.hasText(command.customerData())) {
                request.setCustomerData(command.customerData());
            }
            ChannelCreateFlowByFilesResponse response = client.ChannelCreateFlowByFiles(request);
            if (!StringUtils.hasText(response.getFlowId())) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "创建签署流程成功但未返回 flowId");
            }
            return response.getFlowId();
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("渠道版按文件发起签署失败, flowName={}, code={}, message={}",
                    command.flowName(), ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "创建签署流程失败: " + ex.getMessage(), ex);
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
        List<EssSignComponentCommand> signComponents = command.signComponents();
        if (!CollectionUtils.isEmpty(signComponents)) {
            Component[] components = new Component[signComponents.size()];
            for (int i = 0; i < signComponents.size(); i++) {
                components[i] = toComponent(signComponents.get(i));
            }
            approverInfo.setSignComponents(components);
        }
        return approverInfo;
    }

    private static Component toComponent(EssSignComponentCommand command) {
        Component component = new Component();
        if (command == null) {
            return component;
        }
        if (StringUtils.hasText(command.componentType())) {
            component.setComponentType(command.componentType());
        }
        if (StringUtils.hasText(command.componentValue())) {
            component.setComponentValue(command.componentValue());
        }
        if (command.componentPosX() != null) {
            component.setComponentPosX(command.componentPosX());
        }
        if (command.componentPosY() != null) {
            component.setComponentPosY(command.componentPosY());
        }
        if (command.componentWidth() != null) {
            component.setComponentWidth(command.componentWidth());
        }
        if (command.componentHeight() != null) {
            component.setComponentHeight(command.componentHeight());
        }
        if (command.fileIndex() != null) {
            component.setFileIndex(command.fileIndex());
        }
        if (command.componentPage() != null) {
            component.setComponentPage(command.componentPage());
        }
        if (StringUtils.hasText(command.generateMode())) {
            component.setGenerateMode(command.generateMode());
        }
        if (StringUtils.hasText(command.componentId())) {
            component.setComponentId(command.componentId());
        }
        if (command.offsetX() != null) {
            component.setOffsetX(command.offsetX());
        }
        if (command.offsetY() != null) {
            component.setOffsetY(command.offsetY());
        }
        component.setComponentRequired(Boolean.TRUE);
        return component;
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
