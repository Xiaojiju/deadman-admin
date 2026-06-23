package com.mtfm.deadman.plugin.file.spi;

/**
 * 存储层文件引用，由 Provider 上传后返回。
 *
 * @param providerId    存储提供商标识，如 {@code local}、{@code oss}
 * @param storageKey    存储键（相对路径或对象 Key）
 * @param accessUrl     可直接访问的 URL，无公开地址时为 {@code null}
 * @param storageBucket 物理存储桶名（OSS 等多 Bucket 场景必填；本地磁盘为 {@code null}）
 */
public record StoredFileRef(String providerId, String storageKey, String accessUrl, String storageBucket) {
}
