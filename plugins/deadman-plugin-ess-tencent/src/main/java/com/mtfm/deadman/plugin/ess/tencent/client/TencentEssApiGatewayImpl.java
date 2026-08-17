package com.mtfm.deadman.plugin.ess.tencent.client;

import java.util.Base64;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.ApproverInfo;
import com.tencentcloudapi.ess.v20201111.models.Caller;
import com.tencentcloudapi.ess.v20201111.models.CancelFlowRequest;
import com.tencentcloudapi.ess.v20201111.models.CancelFlowResponse;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowByFilesRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowByFilesResponse;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.DescribeFileUrlsRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFileUrlsResponse;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowInfoRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowInfoResponse;
import com.tencentcloudapi.ess.v20201111.models.FileUrl;
import com.tencentcloudapi.ess.v20201111.models.UploadFile;
import com.tencentcloudapi.ess.v20201111.models.UploadFilesRequest;
import com.tencentcloudapi.ess.v20201111.models.UploadFilesResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯电子签 API 网关实现，封装 ess-java-kit 主流程调用。
 */
@Slf4j
@RequiredArgsConstructor
public class TencentEssApiGatewayImpl implements TencentEssApiGateway {

    private final EssClientFactory essClientFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public String uploadFile(String operatorUserId, String fileName, byte[] fileBytes) {
        if (!StringUtils.hasText(fileName) || fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "电子签上传文件内容不能为空");
        }
        try {
            EssClient client = essClientFactory.createFileClient();
            UploadFilesRequest request = new UploadFilesRequest();
            Caller caller = new Caller();
            caller.setOperatorId(operatorUserId);
            request.setCaller(caller);
            request.setBusinessType("DOCUMENT");

            UploadFile uploadFile = new UploadFile();
            uploadFile.setFileName(fileName);
            uploadFile.setFileBody(Base64.getEncoder().encodeToString(fileBytes));
            request.setFileInfos(new UploadFile[] {uploadFile});

            UploadFilesResponse response = client.UploadFiles(request);
            if (response.getFileIds() == null || response.getFileIds().length == 0) {
                throw new BusinessException(ResultCode.ESS_UPLOAD_FAILED, "电子签上传成功但未返回 fileId");
            }
            return response.getFileIds()[0];
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("腾讯电子签上传文件失败, fileName={}, code={}, message={}",
                fileName, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_UPLOAD_FAILED, "电子签文件上传失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createFlowByFiles(
            String operatorUserId,
            String flowName,
            String fileId,
            ApproverInfo[] approvers,
            Long deadlineUnix,
            Boolean unordered,
            String userData) {
        if (!StringUtils.hasText(flowName) || !StringUtils.hasText(fileId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "流程名称与 fileId 不能为空");
        }
        if (approvers == null || approvers.length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "签署方不能为空");
        }
        try {
            EssClient client = essClientFactory.createApiClient();
            CreateFlowByFilesRequest request = new CreateFlowByFilesRequest();
            request.setOperator(buildOperator(operatorUserId));
            request.setFlowName(flowName);
            request.setFileIds(new String[] {fileId});
            request.setApprovers(approvers);
            if (deadlineUnix != null) {
                request.setDeadline(deadlineUnix);
            }
            if (unordered != null) {
                request.setUnordered(unordered);
            }
            if (StringUtils.hasText(userData)) {
                request.setUserData(userData);
            }
            CreateFlowByFilesResponse response = client.CreateFlowByFiles(request);
            if (!StringUtils.hasText(response.getFlowId())) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "创建签署流程成功但未返回 flowId");
            }
            return response.getFlowId();
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("腾讯电子签创建流程失败, flowName={}, fileId={}, code={}, message={}",
                flowName, fileId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "创建签署流程失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateSchemeUrlResponse createSchemeUrl(String operatorUserId, String flowId) {
        requireFlowId(flowId);
        try {
            EssClient client = essClientFactory.createApiClient();
            CreateSchemeUrlRequest request = new CreateSchemeUrlRequest();
            request.setOperator(buildOperator(operatorUserId));
            request.setFlowId(flowId);
            request.setPathType(1L);
            return client.CreateSchemeUrl(request);
        } catch (TencentCloudSDKException ex) {
            log.error("腾讯电子签获取签署链接失败, flowId={}, code={}, message={}",
                flowId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "获取签署链接失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DescribeFlowInfoResponse describeFlowInfo(String operatorUserId, String flowId) {
        requireFlowId(flowId);
        try {
            EssClient client = essClientFactory.createApiClient();
            DescribeFlowInfoRequest request = new DescribeFlowInfoRequest();
            request.setOperator(buildOperator(operatorUserId));
            request.setFlowIds(new String[] {flowId});
            return client.DescribeFlowInfo(request);
        } catch (TencentCloudSDKException ex) {
            log.error("腾讯电子签查询流程失败, flowId={}, code={}, message={}",
                flowId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "查询签署流程失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describeFileUrl(String operatorUserId, String flowId) {
        requireFlowId(flowId);
        try {
            EssClient client = essClientFactory.createApiClient();
            DescribeFileUrlsRequest request = new DescribeFileUrlsRequest();
            request.setOperator(buildOperator(operatorUserId));
            request.setBusinessType("FLOW");
            request.setBusinessIds(new String[] {flowId});
            DescribeFileUrlsResponse response = client.DescribeFileUrls(request);
            FileUrl[] urls = response.getFileUrls();
            if (urls == null || urls.length == 0 || !StringUtils.hasText(urls[0].getUrl())) {
                throw new BusinessException(ResultCode.ESS_API_FAILED, "未获取到合同下载地址");
            }
            return urls[0].getUrl();
        } catch (BusinessException ex) {
            throw ex;
        } catch (TencentCloudSDKException ex) {
            log.error("腾讯电子签获取下载地址失败, flowId={}, code={}, message={}",
                flowId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "获取合同下载地址失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CancelFlowResponse cancelFlow(String operatorUserId, String flowId, String cancelMessage) {
        requireFlowId(flowId);
        try {
            EssClient client = essClientFactory.createApiClient();
            CancelFlowRequest request = new CancelFlowRequest();
            request.setOperator(buildOperator(operatorUserId));
            request.setFlowId(flowId);
            request.setCancelMessage(StringUtils.hasText(cancelMessage) ? cancelMessage : "业务撤销");
            return client.CancelFlow(request);
        } catch (TencentCloudSDKException ex) {
            log.error("腾讯电子签撤销流程失败, flowId={}, code={}, message={}",
                flowId, ex.getErrorCode(), ex.getMessage());
            throw new BusinessException(ResultCode.ESS_API_FAILED, "撤销签署流程失败: " + ex.getMessage(), ex);
        }
    }

    private static UserInfo buildOperator(String operatorUserId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorUserId);
        return userInfo;
    }

    private static void requireFlowId(String flowId) {
        if (!StringUtils.hasText(flowId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "flowId 不能为空");
        }
    }
}
