package com.mtfm.deadman.plugin.identity.client;

import java.util.Base64;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.identity.support.IdentityErrorCodes;
import com.mtfm.deadman.plugin.identity.config.IdentityPluginProperties;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionResult;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CompareFaceRequest;
import com.tencentcloudapi.iai.v20200303.models.CompareFaceResponse;
import com.tencentcloudapi.iai.v20200303.models.DetectLiveFaceAccurateRequest;
import com.tencentcloudapi.iai.v20200303.models.DetectLiveFaceAccurateResponse;

/**
 * 腾讯云人脸识别：高精度静态活体 {@code DetectLiveFaceAccurate} 与人脸比对 {@code CompareFace}。
 */
public class TencentIaiFaceRecognitionClient implements FaceRecognitionClient {

    private final IdentityPluginProperties properties;
    private final IaiClient client;

    /**
     * 使用插件配置创建 IAI 客户端。
     *
     * @param properties 插件配置
     */
    public TencentIaiFaceRecognitionClient(IdentityPluginProperties properties) {
        this.properties = properties;
        IdentityPluginProperties.Tencent tencent = properties.getTencent();
        Credential credential = new Credential(tencent.getSecretId(), tencent.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("iai.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        this.client = new IaiClient(credential, tencent.getRegion(), clientProfile);
    }

    /**
     * 调用高精度静态活体检测。
     *
     * @param faceImage 人脸图片
     * @return 活体分数
     */
    @Override
    public FaceRecognitionResult detectLive(byte[] faceImage) {
        DetectLiveFaceAccurateRequest request = new DetectLiveFaceAccurateRequest();
        request.setImage(encode(faceImage));
        request.setFaceModelVersion(properties.getTencent().getFaceModelVersion());
        try {
            DetectLiveFaceAccurateResponse response = client.DetectLiveFaceAccurate(request);
            return new FaceRecognitionResult(score(response.getScore()), response.getRequestId());
        } catch (TencentCloudSDKException ex) {
            throw translate(ex);
        }
    }

    /**
     * 调用人脸比对。证件图作为 ImageA，现场人脸作为 ImageB。
     *
     * @param idCardImage 证件图片
     * @param faceImage 现场人脸
     * @return 相似度分数
     */
    @Override
    public FaceRecognitionResult compare(byte[] idCardImage, byte[] faceImage) {
        IdentityPluginProperties.Tencent tencent = properties.getTencent();
        CompareFaceRequest request = new CompareFaceRequest();
        request.setImageA(encode(idCardImage));
        request.setImageB(encode(faceImage));
        request.setFaceModelVersion(tencent.getFaceModelVersion());
        request.setQualityControl((long)tencent.getQualityControl());
        request.setNeedRotateDetection((long)tencent.getNeedRotateDetection());
        try {
            CompareFaceResponse response = client.CompareFace(request);
            return new FaceRecognitionResult(score(response.getScore()), response.getRequestId());
        } catch (TencentCloudSDKException ex) {
            throw translate(ex);
        }
    }

    private static String encode(byte[] image) {
        return Base64.getEncoder().encodeToString(image);
    }

    private static float score(Float value) {
        return value == null ? 0F : value;
    }

    private static BusinessException translate(TencentCloudSDKException ex) {
        String code = ex.getErrorCode() == null ? "" : ex.getErrorCode();
        String message = ex.getMessage() == null ? "人脸识别服务调用失败" : ex.getMessage();
        if (code.startsWith("AuthFailure") || code.startsWith("ResourceUnavailable")) {
            return new BusinessException(IdentityErrorCodes.CONFIG_INVALID, message, ex);
        }
        if (code.contains("NoFaceInPhoto") || code.contains("ImageDecode") || code.contains("ImageEmpty")
            || code.contains("ImageResolution") || code.contains("ImageSize") || code.contains("FaceQuality")
            || code.contains("FaceSize")) {
            return new BusinessException(IdentityErrorCodes.IMAGE_INVALID, message, ex);
        }
        return new BusinessException(IdentityErrorCodes.API_FAILED, message, ex);
    }
}
