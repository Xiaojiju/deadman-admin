package com.mtfm.deadman.plugin.storage.oss.config;

/**
 * OSS 对象公开访问 URL 生成模式。
 */
public enum OssPublicUrlMode {

    /** 通过 CDN 域名拼接直链 */
    CDN,

    /** 通过 OSS 签名 URL 访问 */
    SIGNED
}
