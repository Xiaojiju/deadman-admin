package com.mtfm.deadman.plugin.file.biztype;

import java.util.Collection;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 文件业务分类注册门面，供其他模块在启动时登记允许上传的 {@code bizType}。
 */
@Component
@RequiredArgsConstructor
public class FileBizTypeRegistrar {

    private final FileBizTypeRegistry registry;

    /**
     * 注册单个业务分类。
     *
     * @param bizType 业务分类标识
     */
    public void register(String bizType) {
        registry.register(bizType);
    }

    /**
     * 批量注册业务分类。
     *
     * @param bizTypes 业务分类列表
     */
    public void registerAll(Collection<String> bizTypes) {
        registry.registerAll(bizTypes);
    }
}
