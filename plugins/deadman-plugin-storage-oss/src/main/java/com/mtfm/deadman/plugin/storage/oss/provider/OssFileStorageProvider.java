package com.mtfm.deadman.plugin.storage.oss.provider;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.file.spi.FileStorageProvider;
import com.mtfm.deadman.plugin.file.spi.FileStorageUploadContext;
import com.mtfm.deadman.plugin.file.spi.StoredFileRef;
import com.mtfm.deadman.plugin.storage.oss.client.OssClientFactory;
import com.mtfm.deadman.plugin.storage.oss.config.OssPublicUrlMode;
import com.mtfm.deadman.plugin.storage.oss.config.OssStoragePluginProperties;
import com.mtfm.deadman.plugin.storage.oss.routing.OssBucketResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云 OSS 文件存储 Provider。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssFileStorageProvider implements FileStorageProvider {

    private static final String PROVIDER_ID = "oss";

    private final OssClientFactory ossClientFactory;
    private final OssBucketResolver bucketResolver;
    private final OssStoragePluginProperties properties;

    /**
     * {@inheritDoc}
     */
    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoredFileRef store(FileStorageUploadContext context) {
        String bucket = bucketResolver.resolve(context.getBizType());
        String storageKey = buildStorageKey(context.getBizType(), resolveExtension(context.getOriginalFilename()));
        OSS oss = ossClientFactory.getClient();
        try {
            oss.putObject(bucket, storageKey, context.getInputStream());
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "OSS 文件上传失败");
        }
        String accessUrl = buildAccessUrl(bucket, storageKey);
        return new StoredFileRef(PROVIDER_ID, storageKey, accessUrl, bucket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(StoredFileRef ref) {
        String bucket = requireBucket(ref);
        OSS oss = ossClientFactory.getClient();
        try {
            OSSObject object = oss.getObject(bucket, ref.storageKey());
            return object.getObjectContent();
        } catch (OSSException ex) {
            if ("NoSuchKey".equals(ex.getErrorCode())) {
                throw new BusinessException(ResultCode.FILE_NOT_FOUND);
            }
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "OSS 文件读取失败");
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "OSS 文件读取失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(StoredFileRef ref) {
        String bucket = requireBucket(ref);
        OSS oss = ossClientFactory.getClient();
        try {
            oss.deleteObject(bucket, ref.storageKey());
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "OSS 文件删除失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> publicAccessUrl(StoredFileRef ref) {
        String bucket = requireBucket(ref);
        return Optional.of(buildAccessUrl(bucket, ref.storageKey()));
    }

    private String requireBucket(StoredFileRef ref) {
        if (StringUtils.hasText(ref.storageBucket())) {
            return ref.storageBucket();
        }
        return bucketResolver.resolve(null);
    }

    private String buildAccessUrl(String bucket, String storageKey) {
        OssPublicUrlMode mode = resolveAccessMode(bucket);
        if (mode == OssPublicUrlMode.CDN) {
            String cdnDomain = properties.getCdnDomains().get(bucket);
            if (StringUtils.hasText(cdnDomain)) {
                return normalizeCdnDomain(cdnDomain) + "/" + storageKey;
            }
            // CDN 域名未配置时回退签名 URL
            mode = OssPublicUrlMode.SIGNED;
        }
        return generateSignedUrl(bucket, storageKey);
    }

    private String generateSignedUrl(String bucket, String storageKey) {
        OSS oss = ossClientFactory.getClient();
        Date expiration = new Date(System.currentTimeMillis() + properties.getSignedUrlExpireSeconds() * 1000L);
        try {
            URL url = oss.generatePresignedUrl(bucket, storageKey, expiration);
            return url == null ? null : url.toString();
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "OSS 签名 URL 生成失败");
        }
    }

    private OssPublicUrlMode resolveAccessMode(String bucket) {
        String override = properties.getBucketAccessModes().get(bucket);
        if (StringUtils.hasText(override)) {
            return OssPublicUrlMode.valueOf(override.trim().toUpperCase());
        }
        return properties.getPublicUrlMode();
    }

    private String buildStorageKey(String bizType, String extension) {
        LocalDate today = LocalDate.now();
        String datePath = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String bizSegment = StringUtils.hasText(bizType) ? sanitizeSegment(bizType) + "/" : "";
        String key = bizSegment + datePath + "/" + UUID.randomUUID() + extension;
        String prefix = properties.getPathPrefix();
        if (!StringUtils.hasText(prefix)) {
            return key;
        }
        String normalizedPrefix = prefix.replace("\\", "/");
        if (normalizedPrefix.endsWith("/")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
        }
        return normalizedPrefix + "/" + key;
    }

    private static String normalizeCdnDomain(String cdnDomain) {
        String domain = cdnDomain.trim();
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "https://" + domain;
        }
        return domain;
    }

    /**
     * 解析文件扩展名
     *
     * @param originalFilename 原始文件名
     * @return 文件扩展名
     */
    private static String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex);
    }

    /**
     * 清理文件路径段
     *
     * @param segment 文件路径段
     * @return 清理后的文件路径段
     */
    private static String sanitizeSegment(String segment) {
        return segment.replace("\\", "/").replaceAll("[^a-zA-Z0-9_\\-/]", "_");
    }
}
