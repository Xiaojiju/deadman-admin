package com.mtfm.deadman.plugin.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mtfm.deadman.plugin.sms.config.SmsPluginProperties;

/**
 * 默认短信发送器：不调用外部通道，按配置决定是否在日志中打印验证码。
 */
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    private final SmsPluginProperties properties;

    /**
     * 使用插件配置。
     *
     * @param properties 插件配置
     */
    public LoggingSmsSender(SmsPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 记录发送动作。
     *
     * @param phone 手机号
     * @param scene 场景
     * @param code 验证码
     */
    @Override
    public void send(String phone, String scene, String code) {
        if (properties.isLogCode()) {
            log.info("短信验证码 phone={} scene={} code={}", phone, scene, code);
            return;
        }
        log.info("短信验证码已生成 phone={} scene={}", phone, scene);
    }
}
