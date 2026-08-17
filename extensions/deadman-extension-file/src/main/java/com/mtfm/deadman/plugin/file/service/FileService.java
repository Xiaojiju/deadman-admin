package com.mtfm.deadman.plugin.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.file.biztype.FileBizTypeRegistry;
import com.mtfm.deadman.plugin.file.config.FilePluginProperties;
import com.mtfm.deadman.plugin.file.entity.FileMetadata;
import com.mtfm.deadman.plugin.file.manager.FileStorageProviderManager;
import com.mtfm.deadman.plugin.file.mapper.FileMetadataMapper;
import com.mtfm.deadman.plugin.file.spi.FileStorageProvider;
import com.mtfm.deadman.plugin.file.spi.FileStorageUploadContext;
import com.mtfm.deadman.plugin.file.spi.StoredFileRef;
import com.mtfm.deadman.plugin.file.vo.FileAccessUrlVO;
import com.mtfm.deadman.plugin.file.vo.FileDownloadResource;
import com.mtfm.deadman.plugin.file.vo.FileMetadataVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件上传、下载与元数据管理服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataMapper fileMetadataMapper;
    private final FileStorageProviderManager providerManager;
    private final FilePluginProperties filePluginProperties;
    private final FileBizTypeRegistry fileBizTypeRegistry;

    /**
     * 上传文件。
     *
     * @param file           上传文件
     * @param bizType        业务分类
     * @param providerId     存储 Provider，为空时使用默认
     * @param uploaderUserId 上传人用户 ID
     * @return 文件元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public FileMetadataVO upload(MultipartFile file, String bizType, String providerId, Long uploaderUserId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        long maxBytes = filePluginProperties.getMaxFileSize().toBytes();
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE);
        }
        fileBizTypeRegistry.requireRegistered(bizType);
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            return storeAndPersist(inputStream, originalFilename, file.getContentType(), file.getSize(), bizType,
                providerId, uploaderUserId);
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "读取上传文件失败");
        }
    }

    /**
     * 上传字节内容（服务端生成文件，如小程序码 PNG）。
     *
     * @param content          文件字节
     * @param originalFilename 原始文件名
     * @param contentType      MIME
     * @param bizType          业务分类
     * @param providerId       存储 Provider，为空时使用默认
     * @param uploaderUserId   上传人用户 ID
     * @return 文件元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public FileMetadataVO uploadBytes(
            byte[] content,
            String originalFilename,
            String contentType,
            String bizType,
            String providerId,
            Long uploaderUserId) {
        if (content == null || content.length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        long maxBytes = filePluginProperties.getMaxFileSize().toBytes();
        if (content.length > maxBytes) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE);
        }
        fileBizTypeRegistry.requireRegistered(bizType);
        String safeName = StringUtils.cleanPath(
            StringUtils.hasText(originalFilename) ? originalFilename : "file.bin");
        return storeAndPersist(
            new java.io.ByteArrayInputStream(content),
            safeName,
            contentType,
            content.length,
            bizType,
            providerId,
            uploaderUserId);
    }

    private FileMetadataVO storeAndPersist(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            long size,
            String bizType,
            String providerId,
            Long uploaderUserId) {
        FileStorageProvider provider = providerManager.require(providerId);
        StoredFileRef storedRef = provider.store(FileStorageUploadContext.builder()
                .originalFilename(originalFilename)
                .contentType(contentType)
                .size(size)
                .inputStream(inputStream)
                .bizType(bizType)
                .uploaderUserId(uploaderUserId)
                .build());
        FileMetadata metadata = FileMetadata.builder()
                .fileCode(generateFileCode())
                .originalFilename(originalFilename)
                .contentType(contentType)
                .sizeBytes(size)
                .providerId(storedRef.providerId())
                .storageKey(storedRef.storageKey())
                .storageBucket(storedRef.storageBucket())
                .accessUrl(storedRef.accessUrl())
                .bizType(bizType)
                .uploaderUserId(uploaderUserId)
                .build();
        fileMetadataMapper.insert(metadata);
        return toVo(metadata);
    }

    /**
     * 按主键查询文件元数据（accessUrl 按存储 Provider 动态刷新）。
     *
     * @param fileId 文件主键
     * @return 文件元数据
     */
    public FileMetadataVO getById(Long fileId) {
        return toVo(requireMetadata(fileId));
    }

    /**
     * 按文件 ID 换取当前可访问 URL（根据元数据中的 providerId 路由到对应存储插件）。
     *
     * @param fileId 文件主键
     * @return 访问地址视图
     */
    public FileAccessUrlVO resolveAccessUrl(Long fileId) {
        FileMetadata metadata = requireMetadata(fileId);
        return new FileAccessUrlVO(metadata.getId(), resolveFreshAccessUrl(metadata), metadata.getProviderId());
    }

    /**
     * 批量按文件 ID 换取当前可访问 URL（缺失 ID 跳过，不抛错）。
     *
     * @param fileIds 文件主键集合
     * @return fileId → 访问地址视图
     */
    public Map<Long, FileAccessUrlVO> resolveAccessUrlsAsMap(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Map.of();
        }
        List<Long> distinctIds = fileIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        List<FileMetadata> rows = fileMetadataMapper.selectByIds(distinctIds);
        Map<Long, FileAccessUrlVO> result = new java.util.LinkedHashMap<>();
        for (FileMetadata row : rows) {
            result.put(row.getId(),
                    new FileAccessUrlVO(row.getId(), resolveFreshAccessUrl(row), row.getProviderId()));
        }
        return result;
    }

    /**
     * 批量按文件 ID 换取当前可访问 URL（保持入参顺序；缺失 ID 抛出 FILE_NOT_FOUND）。
     *
     * @param fileIds 文件主键集合
     * @return 访问地址列表
     */
    public List<FileAccessUrlVO> resolveAccessUrls(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        List<Long> orderedIds = new ArrayList<>();
        for (Long fileId : fileIds) {
            if (fileId != null) {
                orderedIds.add(fileId);
            }
        }
        if (orderedIds.isEmpty()) {
            return List.of();
        }
        Map<Long, FileAccessUrlVO> found = resolveAccessUrlsAsMap(orderedIds);
        List<FileAccessUrlVO> result = new ArrayList<>(orderedIds.size());
        for (Long fileId : orderedIds) {
            FileAccessUrlVO url = found.get(fileId);
            if (url == null) {
                throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件不存在：" + fileId);
            }
            result.add(url);
        }
        return result;
    }

    /**
     * 按主键批量查询文件元数据（保持入参顺序；缺失 ID 会抛出 FILE_NOT_FOUND）。
     *
     * @param fileIds 文件主键集合
     * @return 元数据列表
     */
    public List<FileMetadataVO> listByIds(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        List<Long> orderedIds = new ArrayList<>();
        for (Long fileId : fileIds) {
            if (fileId != null) {
                orderedIds.add(fileId);
            }
        }
        if (orderedIds.isEmpty()) {
            return List.of();
        }
        Map<Long, FileMetadataVO> found = findByIdsAsMap(orderedIds);
        List<FileMetadataVO> result = new ArrayList<>(orderedIds.size());
        for (Long fileId : orderedIds) {
            FileMetadataVO meta = found.get(fileId);
            if (meta == null) {
                throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件不存在：" + fileId);
            }
            result.add(meta);
        }
        return result;
    }

    /**
     * 按主键批量查询已存在的文件元数据（缺失 ID 跳过，不抛错；accessUrl 动态刷新）。
     *
     * @param fileIds 文件主键集合
     * @return fileId → 元数据
     */
    public Map<Long, FileMetadataVO> findByIdsAsMap(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Map.of();
        }
        List<Long> distinctIds = fileIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        List<FileMetadata> rows = fileMetadataMapper.selectByIds(distinctIds);
        return rows.stream().collect(Collectors.toMap(FileMetadata::getId, this::toVo, (a, b) -> a));
    }

    /**
     * 打开文件流用于下载。
     *
     * @param fileId 文件主键
     * @return 下载资源
     */
    public FileDownloadResource openDownload(Long fileId) {
        FileMetadata metadata = requireMetadata(fileId);
        FileStorageProvider provider = providerManager.require(metadata.getProviderId());
        StoredFileRef ref = toStoredRef(metadata);
        try {
            InputStream inputStream = provider.open(ref);
            return FileDownloadResource.builder()
                    .originalFilename(metadata.getOriginalFilename())
                    .contentType(metadata.getContentType())
                    .sizeBytes(metadata.getSizeBytes())
                    .inputStream(inputStream)
                    .build();
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "打开文件失败");
        }
    }

    /**
     * 删除文件（逻辑删除元数据并删除存储对象）。
     *
     * @param fileId 文件主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long fileId) {
        FileMetadata metadata = requireMetadata(fileId);
        FileStorageProvider provider = providerManager.require(metadata.getProviderId());
        StoredFileRef ref = toStoredRef(metadata);
        try {
            provider.delete(ref);
        } catch (RuntimeException ex) {
            log.warn("删除存储对象失败，仍继续逻辑删除元数据：fileId={}", fileId, ex);
        }
        fileMetadataMapper.deleteById(fileId);
    }

    /**
     * 列出已注册的文件业务分类。
     *
     * @return 业务分类列表
     */
    public List<String> listBizTypes() {
        return fileBizTypeRegistry.listRegistered();
    }

    /**
     * 列出已注册的存储 Provider。
     *
     * @return Provider 标识列表
     */
    public List<String> listProviders() {
        return providerManager.listProviderIds();
    }

    private FileMetadata requireMetadata(Long fileId) {
        FileMetadata metadata = fileMetadataMapper.selectOne(new LambdaQueryWrapper<FileMetadata>()
                .eq(FileMetadata::getId, fileId));
        if (metadata == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return metadata;
    }

    /**
     * 按元数据中的 provider 动态生成可访问 URL；失败时回退上传时快照。
     * 相对路径在配置了 {@code deadman.plugin.file.public-base-url} 时拼成绝对地址。
     *
     * @param metadata 文件元数据
     * @return 当前可访问 URL
     */
    private String resolveFreshAccessUrl(FileMetadata metadata) {
        String raw;
        try {
            FileStorageProvider provider = providerManager.require(metadata.getProviderId());
            raw = provider.publicAccessUrl(toStoredRef(metadata)).orElse(metadata.getAccessUrl());
        } catch (RuntimeException ex) {
            log.warn("动态刷新文件访问 URL 失败，回退快照：fileId={}, providerId={}",
                    metadata.getId(), metadata.getProviderId(), ex);
            raw = metadata.getAccessUrl();
        }
        return absolutizeAccessUrl(raw);
    }

    /**
     * 将相对 accessUrl 拼成绝对 URL（已是 http(s) 则原样返回）。
     *
     * @param accessUrl Provider 返回的访问地址
     * @return 绝对化后的地址；无法绝对化时返回原值
     */
    private String absolutizeAccessUrl(String accessUrl) {
        if (!StringUtils.hasText(accessUrl)) {
            return accessUrl;
        }
        String trimmed = accessUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String base = filePluginProperties.getPublicBaseUrl();
        if (!StringUtils.hasText(base)) {
            return trimmed;
        }
        String normalizedBase = base.trim();
        while (normalizedBase.endsWith("/")) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }
        if (trimmed.startsWith("/")) {
            return normalizedBase + trimmed;
        }
        return normalizedBase + "/" + trimmed;
    }

    private static StoredFileRef toStoredRef(FileMetadata metadata) {
        return new StoredFileRef(
                metadata.getProviderId(),
                metadata.getStorageKey(),
                metadata.getAccessUrl(),
                metadata.getStorageBucket());
    }

    private FileMetadataVO toVo(FileMetadata metadata) {
        return new FileMetadataVO(
                metadata.getId(),
                metadata.getFileCode(),
                metadata.getOriginalFilename(),
                metadata.getContentType(),
                metadata.getSizeBytes(),
                metadata.getProviderId(),
                resolveFreshAccessUrl(metadata),
                metadata.getBizType(),
                metadata.getUploaderUserId(),
                metadata.getCreateTime());
    }

    private static String generateFileCode() {
        return "F" + UUID.randomUUID().toString().replace("-", "");
    }
}
