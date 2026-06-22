package com.mtfm.deadman.plugin.storage.oss.client;

import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.storage.oss.config.OssStoragePluginProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云 OSS 客户端工厂，负责创建与销毁 SDK 客户端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssClientFactory {

    private final OssStoragePluginProperties properties;

    private volatile OSS ossClient;

    /**
     * 获取 OSS 客户端（懒加载单例）。
     *
     * @return OSS 客户端
     */
    public OSS getClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    ossClient = createClient();
                }
            }
        }
        return ossClient;
    }

    /**
     * 关闭 OSS 客户端，释放连接资源。
     */
    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            try {
                ossClient.shutdown();
            } catch (RuntimeException ex) {
                log.warn("关闭 OSS 客户端失败", ex);
            } finally {
                ossClient = null;
            }
        }
    }

    /**
     * 创建 OSS 客户端。
     *
     * @return OSS 客户端
     */
    private OSS createClient() {
        if (!StringUtils.hasText(properties.getEndpoint())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "OSS endpoint 未配置");
        }
        if (!StringUtils.hasText(properties.getAccessKeyId())
                || !StringUtils.hasText(properties.getAccessKeySecret())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "OSS 访问密钥未配置");
        }
        return new OSSClientBuilder()
                .build(properties.getEndpoint(), properties.getAccessKeyId(), properties.getAccessKeySecret());
    }
}
