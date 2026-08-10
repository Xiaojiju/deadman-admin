package com.mtfm.deadman.plugin.wechat.miniprogram.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 获取不限制的小程序码请求参数。
 * <p>
 * 对应微信接口 getUnlimitedQRCode（/wxa/getwxacodeunlimit）。
 *
 * @param scene       场景值，最大 32 个可见字符；扫码进入小程序后可通过 query.scene 获取
 * @param page        页面路径，如 pages/index/index；根路径前不要加 /，不能带参数
 * @param checkPath   是否检查 page 已在现网发布，默认 true
 * @param envVersion  打开的小程序版本：release / trial / develop，默认 release
 * @param width       二维码宽度（px），默认 430，范围 280–1280
 * @param autoColor   是否自动配置线条颜色，默认 false
 * @param lineColor   线条颜色 RGB；仅当 autoColor 为 false 时生效
 * @param hyaline     是否需要透明底色，默认 false
 */
public record WechatUnlimitedQrCodeRequest(
        @NotBlank @Size(max = 32) String scene,
        String page,
        Boolean checkPath,
        String envVersion,
        @Min(280) @Max(1280) Integer width,
        Boolean autoColor,
        WechatQrCodeLineColor lineColor,
        Boolean hyaline) {

    /**
     * 仅指定 scene 的便捷构造（其余参数走微信默认值）。
     *
     * @param scene 场景值
     * @return 请求对象
     */
    public static WechatUnlimitedQrCodeRequest ofScene(String scene) {
        return new WechatUnlimitedQrCodeRequest(scene, null, null, null, null, null, null, null);
    }

    /**
     * 小程序码线条颜色（RGB 十进制）。
     *
     * @param r 红色分量 0–255
     * @param g 绿色分量 0–255
     * @param b 蓝色分量 0–255
     */
    public record WechatQrCodeLineColor(
            @Min(0) @Max(255) int r, @Min(0) @Max(255) int g, @Min(0) @Max(255) int b) {
    }
}
