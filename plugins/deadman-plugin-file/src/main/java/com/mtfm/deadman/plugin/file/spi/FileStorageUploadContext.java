package com.mtfm.deadman.plugin.file.spi;

import java.io.InputStream;
import lombok.Builder;
import lombok.Getter;

/**
 * 文件上传上下文，传递给存储 Provider。
 */
@Getter
@Builder
public class FileStorageUploadContext {

    /** 原始文件名 */
    private final String originalFilename;

    /** MIME 类型 */
    private final String contentType;

    /** 文件大小（字节） */
    private final long size;

    /** 文件输入流（由调用方关闭） */
    private final InputStream inputStream;

    /** 业务分类（可用于子目录划分） */
    private final String bizType;

    /** 上传人用户 ID */
    private final Long uploaderUserId;
}
