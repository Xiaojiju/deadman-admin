package com.mtfm.deadman.plugin.storage.cos.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 腾讯云 COS 存储插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.storage-cos")
public class CosStoragePluginProperties {

    /** 是否启用插件 */
    private boolean enabled = false;

    /** COS 地域，如 ap-guangzhou */
    private String region;

    /** 腾讯云 API 密钥 SecretId */
    private String secretId;

    /** 腾讯云 API 密钥 SecretKey */
    private String secretKey;

    /** 默认 Bucket（未命中 bizType 路由时使用） */
    private String defaultBucket;

    /** bizType → Bucket 路由映射 */
    private Map<String, String> bucketRouting = new HashMap<>();

    /** Bucket → CDN 根域名（不含末尾斜杠） */
    private Map<String, String> cdnDomains = new HashMap<>();

    /** Bucket → 公开访问模式覆盖（cdn / signed） */
    private Map<String, String> bucketAccessModes = new HashMap<>();

    /** 全局默认公开访问模式 */
    private CosPublicUrlMode publicUrlMode = CosPublicUrlMode.CDN;

    /** 签名 URL 过期秒数 */
    private long signedUrlExpireSeconds = 3600;

    /** 对象 Key 全局前缀（可选） */
    private String pathPrefix = "";
}
