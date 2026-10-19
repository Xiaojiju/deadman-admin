package com.mtfm.deadman.plugin.sms;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mtfm.deadman.common.validation.PhoneNumber;
import com.mtfm.deadman.plugin.sms.config.SmsPluginProperties;

/**
 * 生成、限流并校验手机验证码。不绑定具体用户体系。
 */
public class SmsCodeService {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeService.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final AtomicBoolean FIXED_CODE_WARNED = new AtomicBoolean();

    private final SmsCodeStore smsCodeStore;
    private final SmsSender smsSender;
    private final SmsPluginProperties properties;

    /**
     * 组装验证码服务。
     *
     * @param smsCodeStore 验证码存储
     * @param smsSender 短信发送器
     * @param properties 插件配置
     */
    public SmsCodeService(SmsCodeStore smsCodeStore, SmsSender smsSender, SmsPluginProperties properties) {
        this.smsCodeStore = smsCodeStore;
        this.smsSender = smsSender;
        this.properties = properties;
    }

    /**
     * 向手机号发送指定场景的验证码。
     *
     * @param scene 场景
     * @param phone 手机号
     */
    public void send(String scene, String phone) {
        String normalized = requirePhone(phone);
        if (!smsCodeStore.markIfAbsent(limitKey(scene, normalized), properties.getResendInterval())) {
            throw SmsMessages.of(SmsErrorCodes.TOO_FREQUENT, "sms.code.too_frequent", "验证码发送过于频繁");
        }
        String code = nextCode();
        smsSender.send(normalized, scene, code);
        smsCodeStore.save(codeKey(scene, normalized), code, properties.getCodeTtl());
    }

    /**
     * 校验并消费验证码。
     *
     * @param scene 场景
     * @param phone 手机号
     * @param code 验证码
     */
    public void consume(String scene, String phone, String code) {
        String normalized = requirePhone(phone);
        if (code == null || code.isBlank() || !smsCodeStore.consumeIfMatches(codeKey(scene, normalized), code.trim())) {
            throw SmsMessages.of(SmsErrorCodes.CODE_INVALID, "sms.code.invalid", "验证码错误或已过期");
        }
    }

    /**
     * 开启固定码时返回配置值，否则按位数随机生成。
     */
    private String nextCode() {
        if (properties.isFixedCodeEnabled()) {
            String fixed = properties.getFixedCode();
            if (fixed != null && !fixed.isBlank()) {
                if (FIXED_CODE_WARNED.compareAndSet(false, true)) {
                    log.warn("短信正在使用固定验证码，上线后请关闭 deadman.plugin.sms.fixed-code-enabled");
                }
                return fixed.trim();
            }
            log.warn("已开启固定验证码，但 fixed-code 为空，改为随机生成");
        }
        return randomCode(properties.getCodeLength());
    }

    private static String randomCode(int length) {
        int digits = Math.max(4, Math.min(length, 8));
        int bound = (int)Math.pow(10, digits);
        int floor = bound / 10;
        return String.valueOf(floor + RANDOM.nextInt(bound - floor));
    }

    private static String requirePhone(String phone) {
        if (phone == null || !phone.trim().matches(PhoneNumber.PATTERN)) {
            throw SmsMessages.invalidPhone();
        }
        return phone.trim();
    }

    private static String codeKey(String scene, String phone) {
        return "deadman:sms:code:" + scene + ":" + phone;
    }

    private static String limitKey(String scene, String phone) {
        return "deadman:sms:limit:" + scene + ":" + phone;
    }
}
