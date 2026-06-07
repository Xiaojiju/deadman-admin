package com.mtfm.deadman.plugin.storage.local.provider;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.file.spi.FileStorageProvider;
import com.mtfm.deadman.plugin.file.spi.FileStorageUploadContext;
import com.mtfm.deadman.plugin.file.spi.StoredFileRef;
import com.mtfm.deadman.plugin.storage.local.config.LocalStoragePluginProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 本地磁盘文件存储 Provider，文件落盘至配置目录并生成公开访问 URL。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileStorageProvider implements FileStorageProvider {

    private static final String PROVIDER_ID = "local";

    private final LocalStoragePluginProperties properties;

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
        String extension = resolveExtension(context.getOriginalFilename());
        String storageKey = buildStorageKey(context.getBizType(), extension);
        Path targetPath = resolveBasePath().resolve(storageKey);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(context.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "本地文件写入失败");
        }
        String accessUrl = buildAccessUrl(storageKey);
        return new StoredFileRef(PROVIDER_ID, storageKey, accessUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(StoredFileRef ref) {
        Path filePath = resolveBasePath().resolve(ref.storageKey());
        if (!Files.exists(filePath)) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        try {
            return Files.newInputStream(filePath);
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "本地文件读取失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(StoredFileRef ref) {
        Path filePath = resolveBasePath().resolve(ref.storageKey());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "本地文件删除失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> publicAccessUrl(StoredFileRef ref) {
        return Optional.ofNullable(ref.accessUrl()).filter(url -> !url.isBlank());
    }

    /**
     * 获取存储根目录绝对路径。
     *
     * @return 根目录路径
     */
    public Path resolveBasePath() {
        return Path.of(properties.getBasePath()).toAbsolutePath().normalize();
    }

    private String buildStorageKey(String bizType, String extension) {
        LocalDate today = LocalDate.now();
        String datePath = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String bizSegment = StringUtils.hasText(bizType) ? sanitizeSegment(bizType) + "/" : "";
        return bizSegment + datePath + "/" + UUID.randomUUID() + extension;
    }

    private String buildAccessUrl(String storageKey) {
        String prefix = properties.getPublicUrlPrefix();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/" + storageKey;
    }

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

    private static String sanitizeSegment(String segment) {
        return segment.replace("\\", "/").replaceAll("[^a-zA-Z0-9_\\-/]", "_");
    }
}
