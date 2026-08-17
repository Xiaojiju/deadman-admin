package com.mtfm.deadman.plugin.file.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 批量按文件 ID 换取访问 URL 请求。
 *
 * @param fileIds 文件主键列表
 */
public record ResolveFileUrlsRequest(
        @NotEmpty(message = "文件 ID 列表不能为空")
        @Size(max = 200, message = "单次最多查询 200 个文件")
        List<Long> fileIds) {
}
