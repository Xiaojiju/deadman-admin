package com.mtfm.deadman.plugin.file.vo;

/**
 * 文件引用视图（业务出参常用：fileId + 动态刷新后的访问 URL）。
 *
 * @param fileId    文件主键
 * @param accessUrl 可直接访问的 URL
 */
public record FileRefVO(Long fileId, String accessUrl) {
}
