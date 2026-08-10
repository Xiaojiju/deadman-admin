package com.mtfm.deadman.plugin.wechat.miniprogram.service;

import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatUnlimitedQrCodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * 微信小程序码服务，封装 getUnlimitedQRCode 调用。
 * <p>
 * 文档：
 * <a href="https://developers.weixin.qq.com/miniprogram/dev/server/API/qrcode-link/qr-code/api_getunlimitedqrcode.html">
 * 获取不限制的小程序码
 * </a>
 */
@Service
@RequiredArgsConstructor
public class WechatQrCodeService {

    private final WechatApiClient wechatApiClient;

    /**
     * 获取不限制的小程序码图片二进制。
     *
     * @param request 小程序码参数（scene 必填）
     * @return 图片 Buffer（通常为 PNG）
     */
    public byte[] getUnlimitedQrCode(WechatUnlimitedQrCodeRequest request) {
        return wechatApiClient.getUnlimitedQrCode(request);
    }

    /**
     * 获取不限制的小程序码，并以 Base64 字符串返回（便于 JSON API 直接下发）。
     *
     * @param request 小程序码参数（scene 必填）
     * @return Base64 编码的图片内容（不含 data URL 前缀）
     */
    public String getUnlimitedQrCodeBase64(WechatUnlimitedQrCodeRequest request) {
        return Base64.getEncoder().encodeToString(getUnlimitedQrCode(request));
    }
}
