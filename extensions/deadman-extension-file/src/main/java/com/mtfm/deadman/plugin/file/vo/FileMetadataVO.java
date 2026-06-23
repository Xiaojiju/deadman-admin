package com.mtfm.deadman.plugin.file.vo;

import java.time.LocalDateTime;

/**
 * 文件元数据视图。
 *
 * @param id               文件主键
 * @param fileCode         文件编码
 * @param originalFilename 原始文件名
 * @param contentType      MIME 类型
 * @param sizeBytes        文件大小（字节）
 * @param providerId       存储 Provider 标识
 * @param accessUrl        可直接访问的 URL
 * @param bizType          业务分类
 * @param uploaderUserId   上传人用户 ID
 * @param createTime       创建时间
 */
public record FileMetadataVO(
        Long id,
        String fileCode,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        String providerId,
        String accessUrl,
        String bizType,
        Long uploaderUserId,
        LocalDateTime createTime) {
}
