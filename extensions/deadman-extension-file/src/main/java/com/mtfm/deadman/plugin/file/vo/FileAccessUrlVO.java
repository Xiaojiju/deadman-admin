package com.mtfm.deadman.plugin.file.vo;

/**
 * 文件访问地址视图（按存储 Provider 动态生成，签名 URL 每次刷新）。
 *
 * @param fileId    文件主键
 * @param accessUrl 可直接访问的 URL
 * @param providerId 存储 Provider 标识
 */
public record FileAccessUrlVO(
        Long fileId,
        String accessUrl,
        String providerId) {
}
