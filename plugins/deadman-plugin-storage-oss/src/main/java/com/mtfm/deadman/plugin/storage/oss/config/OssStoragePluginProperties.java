package com.mtfm.deadman.plugin.storage.oss.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 阿里云 OSS 存储插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.storage-oss")
public class OssStoragePluginProperties {

    /** 是否启用插件 */
    private boolean enabled = false;

    /** OSS 服务端点，如 oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;

    /** 访问密钥 ID */
    private String accessKeyId;

    /** 访问密钥 Secret */
    private String accessKeySecret;

    /** 默认 Bucket（未命中 bizType 路由时使用） */
    private String defaultBucket;

    /** bizType → Bucket 路由映射 */
    private Map<String, String> bucketRouting = new HashMap<>();

    /** Bucket → CDN 根域名（不含末尾斜杠） */
    private Map<String, String> cdnDomains = new HashMap<>();

    /** Bucket → 公开访问模式覆盖（cdn / signed） */
    private Map<String, String> bucketAccessModes = new HashMap<>();

    /** 全局默认公开访问模式 */
    private OssPublicUrlMode publicUrlMode = OssPublicUrlMode.CDN;

    /** 签名 URL 过期秒数 */
    private long signedUrlExpireSeconds = 3600;

    /** 对象 Key 全局前缀（可选） */
    private String pathPrefix = "";
}
