package com.mtfm.deadman.plugin.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 实名认证插件配置。关闭 {@link #enabled} 后整套认证流程不装配；关闭 {@link #realNameEnabled} 后仍可查询历史，但拒绝新的个人实名。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.identity")
public class IdentityPluginProperties {

    /** 是否装配认证插件 */
    private boolean enabled = false;

    /** 是否允许发起个人实名认证。注册流程本身不强制实名，由业务侧读取 {@link #requiredOnRegister} 决定是否引导 */
    private boolean realNameEnabled = true;

    /** 是否允许发起企业认证 */
    private boolean enterpriseEnabled = true;

    /**
     * 注册时是否要求先完成实名。插件不拦截注册接口，只把该开关暴露给业务侧。 当前用户端注册默认不强制。
     */
    private boolean requiredOnRegister = false;

    /**
     * 人脸比对认定为同一人的最低分（含）。 腾讯云人脸比对 3.0 文档中分数为 0–100，万分之一误识率约 50 分；本插件默认 70。
     */
    private float matchScoreThreshold = 70F;

    /**
     * 静态活体通过的最低分（含）。腾讯云高精度活体推荐阈值为 40，取值 0–100。
     */
    private float livenessScoreThreshold = 40F;

    /** 腾讯云人脸识别接入参数 */
    private Tencent tencent = new Tencent();

    /**
     * 腾讯云人脸识别（IAI）接入参数。
     */
    @Data
    public static class Tencent {

        /** 腾讯云 SecretId，建议用环境变量 TENCENT_IAI_SECRET_ID */
        private String secretId;

        /** 腾讯云 SecretKey，建议用环境变量 TENCENT_IAI_SECRET_KEY */
        private String secretKey;

        /** 地域，人脸比对支持 ap-guangzhou 等，见腾讯云地域列表 */
        private String region = "ap-guangzhou";

        /** 算法模型版本，2020-11-26 后开通的账号仅支持 3.0 */
        private String faceModelVersion = "3.0";

        /** 图片质量控制，0 表示不控制，取值 0–4 */
        private int qualityControl = 0;

        /** 是否开启旋转识别：0 关闭，1 开启 */
        private int needRotateDetection = 0;
    }
}
