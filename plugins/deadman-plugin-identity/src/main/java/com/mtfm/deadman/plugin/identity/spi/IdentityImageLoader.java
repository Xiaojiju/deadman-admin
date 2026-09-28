package com.mtfm.deadman.plugin.identity.spi;

/**
 * 按文件主键读取认证图片。由业务组件实现，插件不绑定具体存储。
 */
public interface IdentityImageLoader {

    /**
     * 读取图片字节。
     *
     * @param fileId 文件主键
     * @return 图片原始字节
     */
    byte[] load(Long fileId);
}
