package com.mtfm.deadman.plugin.ess.tencent.client;

import com.tencentcloudapi.ess.v20201111.models.ApproverInfo;
import com.tencentcloudapi.ess.v20201111.models.CancelFlowResponse;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowInfoResponse;

/**
 * 腾讯电子签 API 网关。
 */
public interface TencentEssApiGateway {

    /**
     * 上传合同文件，返回文件资源 Id。
     *
     * @param operatorUserId 经办人 UserId
     * @param fileName 文件名
     * @param fileBytes 文件内容
     * @return fileId
     */
    String uploadFile(String operatorUserId, String fileName, byte[] fileBytes);

    /**
     * 通过已上传文件创建签署流程。
     *
     * @param operatorUserId 经办人 UserId
     * @param flowName 流程名称
     * @param fileId 文件资源 Id
     * @param approvers 签署方
     * @param deadlineUnix 截止时间（Unix 秒），可空
     * @param unordered 是否无序签署，可空
     * @param userData 透传数据，可空
     * @return flowId
     */
    String createFlowByFiles(
            String operatorUserId,
            String flowName,
            String fileId,
            ApproverInfo[] approvers,
            Long deadlineUnix,
            Boolean unordered,
            String userData);

    /**
     * 创建签署链接。
     *
     * @param operatorUserId 经办人 UserId
     * @param flowId 流程 Id
     * @return 签署链接响应
     */
    CreateSchemeUrlResponse createSchemeUrl(String operatorUserId, String flowId);

    /**
     * 查询流程详情。
     *
     * @param operatorUserId 经办人 UserId
     * @param flowId 流程 Id
     * @return 流程详情
     */
    DescribeFlowInfoResponse describeFlowInfo(String operatorUserId, String flowId);

    /**
     * 获取流程对应合同文件下载地址。
     *
     * @param operatorUserId 经办人 UserId
     * @param flowId 流程 Id
     * @return 下载 URL
     */
    String describeFileUrl(String operatorUserId, String flowId);

    /**
     * 撤销签署流程。
     *
     * @param operatorUserId 经办人 UserId
     * @param flowId 流程 Id
     * @param cancelMessage 撤销原因
     * @return 撤销响应
     */
    CancelFlowResponse cancelFlow(String operatorUserId, String flowId, String cancelMessage);
}
