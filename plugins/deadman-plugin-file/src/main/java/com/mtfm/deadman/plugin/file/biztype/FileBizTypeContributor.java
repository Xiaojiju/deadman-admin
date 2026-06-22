package com.mtfm.deadman.plugin.file.biztype;

import java.util.Collection;

/**
 * 文件业务分类贡献者：各业务模块注册本模块使用的 {@code bizType}。
 * <p>
 * 实现类注册为 Spring Bean 后，由 {@link FileBizTypeRegistry} 在应用启动完成后自动聚合。
 */
public interface FileBizTypeContributor {

    /**
     * 贡献本模块允许上传使用的业务分类标识。
     *
     * @return 业务分类列表，可为空
     */
    Collection<String> contribute();
}
