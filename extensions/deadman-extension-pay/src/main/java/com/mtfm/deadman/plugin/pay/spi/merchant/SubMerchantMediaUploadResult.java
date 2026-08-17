package com.mtfm.deadman.plugin.pay.spi.merchant;

/**
 * 二级商户进件媒体上传结果。
 *
 * @param mediaId 渠道返回的媒体文件标识（微信为 MediaID）
 */
public record SubMerchantMediaUploadResult(String mediaId) {
}
