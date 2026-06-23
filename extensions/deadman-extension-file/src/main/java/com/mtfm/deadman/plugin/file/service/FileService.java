package com.mtfm.deadman.plugin.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

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
        FileStorageProvider provider = providerManager.require(providerId);
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename());
        StoredFileRef storedRef;
        try (InputStream inputStream = file.getInputStream()) {
            storedRef = provider.store(FileStorageUploadContext.builder()
                    .originalFilename(originalFilename)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .inputStream(inputStream)
                    .bizType(bizType)
                    .uploaderUserId(uploaderUserId)
                    .build());
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.FILE_STORAGE_ERROR, "读取上传文件失败");
        }
        FileMetadata metadata = FileMetadata.builder()
                .fileCode(generateFileCode())
                .originalFilename(originalFilename)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
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
     * 按主键查询文件元数据。
     *
     * @param fileId 文件主键
     * @return 文件元数据
     */
    public FileMetadataVO getById(Long fileId) {
        return toVo(requireMetadata(fileId));
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
        StoredFileRef ref = new StoredFileRef(
                metadata.getProviderId(),
                metadata.getStorageKey(),
                metadata.getAccessUrl(),
                metadata.getStorageBucket());
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
        StoredFileRef ref = new StoredFileRef(
                metadata.getProviderId(),
                metadata.getStorageKey(),
                metadata.getAccessUrl(),
                metadata.getStorageBucket());
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

    private static FileMetadataVO toVo(FileMetadata metadata) {
        return new FileMetadataVO(
                metadata.getId(),
                metadata.getFileCode(),
                metadata.getOriginalFilename(),
                metadata.getContentType(),
                metadata.getSizeBytes(),
                metadata.getProviderId(),
                metadata.getAccessUrl(),
                metadata.getBizType(),
                metadata.getUploaderUserId(),
                metadata.getCreateTime());
    }

    private static String generateFileCode() {
        return "F" + UUID.randomUUID().toString().replace("-", "");
    }
}
