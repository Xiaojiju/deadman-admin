package com.mtfm.deadman.plugin.file.spi;

/**
 * 存储层文件引用，由 Provider 上传后返回。
 *
 * @param providerId  存储提供商标识，如 {@code local}
 * @param storageKey  存储键（相对路径或对象 Key）
 * @param accessUrl   可直接访问的 URL，无公开地址时为 {@code null}
 */
public record StoredFileRef(String providerId, String storageKey, String accessUrl) {}
