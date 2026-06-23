package com.mtfm.deadman.plugin.file.spi;

import java.io.InputStream;
import java.util.Optional;

/**
 * 文件存储 Provider SPI，各对象存储插件实现此接口并注册为 Spring Bean。
 */
public interface FileStorageProvider {

    /**
     * 提供商标识，全局唯一，如 {@code local}、{@code oss}。
     *
     * @return 提供商标识
     */
    String providerId();

    /**
     * 是否支持指定提供商标识。
     *
     * @param providerId 提供商标识
     * @return 是否支持
     */
    default boolean supports(String providerId) {
        return providerId().equals(providerId);
    }

    /**
     * 存储上传文件。
     *
     * @param context 上传上下文
     * @return 存储引用
     */
    StoredFileRef store(FileStorageUploadContext context);

    /**
     * 打开文件输入流用于下载。
     *
     * @param ref 存储引用
     * @return 文件流（由调用方关闭）
     */
    InputStream open(StoredFileRef ref);

    /**
     * 删除存储对象。
     *
     * @param ref 存储引用
     */
    void delete(StoredFileRef ref);

    /**
     * 获取可直接访问的公开 URL。
     *
     * @param ref 存储引用
     * @return 公开 URL
     */
    Optional<String> publicAccessUrl(StoredFileRef ref);
}
