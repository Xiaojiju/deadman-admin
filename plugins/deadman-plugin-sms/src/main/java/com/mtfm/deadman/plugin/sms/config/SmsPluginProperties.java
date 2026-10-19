package com.mtfm.deadman.plugin.sms.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 短信验证码插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.sms")
public class SmsPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** 验证码有效期 */
    private Duration codeTtl = Duration.ofMinutes(5);

    /** 同一手机号、同一场景的再次发送间隔 */
    private Duration resendInterval = Duration.ofSeconds(60);

    /** 验证码位数 */
    private int codeLength = 6;

<<<<<<< HEAD
    /** 是否发送固定验证码。上线后必须关闭，关闭后恢复随机码 */
    private boolean fixedCodeEnabled = false;

    /** 固定验证码，仅 fixedCodeEnabled 为 true 且非空时使用 */
    private String fixedCode = "";

=======
>>>>>>> refs/remotes/origin/develop-1.0.1
    /** 默认日志发送器是否打印验证码。接入真实通道后应关闭 */
    private boolean logCode = true;
}
