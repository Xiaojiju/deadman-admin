package com.mtfm.deadman.plugin.file.vo;

import java.io.InputStream;
import lombok.Builder;
import lombok.Getter;

/**
 * 文件下载资源封装。
 */
@Getter
@Builder
public class FileDownloadResource {

    /** 原始文件名 */
    private final String originalFilename;

    /** MIME 类型 */
    private final String contentType;

    /** 文件大小（字节） */
    private final Long sizeBytes;

    /** 文件输入流 */
    private final InputStream inputStream;
}
