package com.mtfm.deadman.plugin.storage.cos.client;

import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.storage.cos.config.CosStoragePluginProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云 COS 客户端工厂，负责创建与销毁 SDK 客户端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CosClientFactory {

    private final CosStoragePluginProperties properties;

    private volatile COSClient cosClient;

    /**
     * 获取 COS 客户端（懒加载单例）。
     *
     * @return COS 客户端
     */
    public COSClient getClient() {
        if (cosClient == null) {
            synchronized (this) {
                if (cosClient == null) {
                    cosClient = createClient();
                }
            }
        }
        return cosClient;
    }

    /**
     * 关闭 COS 客户端，释放连接资源。
     */
    @PreDestroy
    public void shutdown() {
        if (cosClient != null) {
            try {
                cosClient.shutdown();
            } catch (RuntimeException ex) {
                log.warn("关闭 COS 客户端失败", ex);
            } finally {
                cosClient = null;
            }
        }
    }

    /**
     * 创建 COS 客户端。
     *
     * @return COS 客户端
     */
    private COSClient createClient() {
        if (!StringUtils.hasText(properties.getRegion())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "COS region 未配置");
        }
        if (!StringUtils.hasText(properties.getSecretId())
                || !StringUtils.hasText(properties.getSecretKey())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "COS 访问密钥未配置");
        }
        COSCredentials credentials = new BasicCOSCredentials(properties.getSecretId(), properties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(properties.getRegion()));
        return new COSClient(credentials, clientConfig);
    }
}
