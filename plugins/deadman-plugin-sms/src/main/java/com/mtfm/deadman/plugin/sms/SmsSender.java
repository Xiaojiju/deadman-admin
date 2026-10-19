package com.mtfm.deadman.plugin.sms;

/**
 * 短信下发 SPI。默认实现只写日志；接入腾讯云短信或其他通道时提供自己的 Bean 即可覆盖。
 */
public interface SmsSender {

    /**
     * 发送验证码。
     *
     * @param phone 手机号
     * @param scene 场景
     * @param code 验证码
     */
    void send(String phone, String scene, String code);
}
