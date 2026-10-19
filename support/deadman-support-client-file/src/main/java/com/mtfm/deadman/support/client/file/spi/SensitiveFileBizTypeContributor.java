package com.mtfm.deadman.support.client.file.spi;

import java.util.Collection;

/**
 * 声明哪些文件业务分类属于敏感的证件类文件（身份证、人脸照、营业执照等）。
 * <ul>
 * <li>用户端只有上传者本人能拿到这类文件的地址和内容，其他人一律按「文件不存在或无权访问」处理。</li>
 * <li>本地存储的静态路径不再公开，管理端拿到的是带过期时间的签名地址。</li>
 * </ul>
 * 头像、轮播、资讯图等公开文件不要放进来。
 */
public interface SensitiveFileBizTypeContributor {

    /**
     * 敏感的业务分类编码，与上传时的 bizType 一致，大小写不敏感。
     *
     * @return 业务分类编码
     */
    Collection<String> sensitiveBizTypes();
}
