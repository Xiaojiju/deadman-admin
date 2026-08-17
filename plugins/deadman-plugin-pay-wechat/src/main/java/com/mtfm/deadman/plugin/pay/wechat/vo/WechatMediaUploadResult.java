package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信媒体文件上传结果。
 *
 * @param mediaId 微信返回的 MediaID（进件证件/执照等字段使用）
 * @see <a href="https://pay.weixin.qq.com/doc/v3/partner/4012760432">文件上传</a>
 */
public record WechatMediaUploadResult(String mediaId) {
}
