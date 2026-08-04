package com.mtfm.deadman.plugin.storage.cos.routing;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.storage.cos.config.CosStoragePluginProperties;

import lombok.RequiredArgsConstructor;

/**
 * 按 bizType 解析目标 COS Bucket。
 */
@Component
@RequiredArgsConstructor
public class CosBucketResolver {

    private final CosStoragePluginProperties properties;

    /**
     * 根据业务分类解析 Bucket 名称。
     *
     * @param bizType 业务分类，可为空
     * @return Bucket 名称
     */
    public String resolve(String bizType) {
        if (StringUtils.hasText(bizType)) {
            String routed = properties.getBucketRouting().get(bizType);
            if (StringUtils.hasText(routed)) {
                return routed;
            }
        }
        String defaultBucket = properties.getDefaultBucket();
        if (!StringUtils.hasText(defaultBucket)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "COS 默认 Bucket 未配置");
        }
        return defaultBucket;
    }
}
