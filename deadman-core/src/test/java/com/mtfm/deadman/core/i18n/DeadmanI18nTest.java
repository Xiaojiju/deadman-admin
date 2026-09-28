package com.mtfm.deadman.core.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.i18n.DeadmanLocales;
import com.mtfm.deadman.common.i18n.MessageSourceHolder;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.common.spi.MessageBasenameContributor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * 多模块文案注册与四种语言解析。
 */
class DeadmanI18nTest {

    @BeforeEach
    void setUp() {
        DeadmanMessageSources.create(DeadmanMessageSources.collectBasenames(List.of()));
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
        MessageSourceHolder.bind(null);
    }

    @Test
    void shouldResolveAcceptLanguage() {
        assertThat(DeadmanLocales.resolve(null)).isEqualTo(DeadmanLocales.SIMPLIFIED_CHINESE);
        assertThat(DeadmanLocales.resolve("")).isEqualTo(DeadmanLocales.SIMPLIFIED_CHINESE);
        assertThat(DeadmanLocales.resolve("zh-CN")).isEqualTo(DeadmanLocales.SIMPLIFIED_CHINESE);
        assertThat(DeadmanLocales.resolve("zh-Hans")).isEqualTo(DeadmanLocales.SIMPLIFIED_CHINESE);
        assertThat(DeadmanLocales.resolve("zh-TW")).isEqualTo(DeadmanLocales.TRADITIONAL_CHINESE);
        assertThat(DeadmanLocales.resolve("zh-HK,zh;q=0.9")).isEqualTo(DeadmanLocales.TRADITIONAL_CHINESE);
        assertThat(DeadmanLocales.resolve("zh-Hant")).isEqualTo(DeadmanLocales.TRADITIONAL_CHINESE);
        assertThat(DeadmanLocales.resolve("bo-CN")).isEqualTo(DeadmanLocales.TIBETAN);
        assertThat(DeadmanLocales.resolve("en-US,en;q=0.9")).isEqualTo(DeadmanLocales.ENGLISH);
        assertThat(DeadmanLocales.resolve("fr-FR")).isEqualTo(DeadmanLocales.SIMPLIFIED_CHINESE);
        assertThat(DeadmanLocales.resolve("not a language tag!!!")).isEqualTo(DeadmanLocales.SIMPLIFIED_CHINESE);
    }

    @Test
    void shouldScanModuleBundlesAndKeepContributorBasenames() {
        List<String> basenames = DeadmanMessageSources.collectBasenames(List.of(new MessageBasenameContributor() {
            @Override
            public List<String> basenames() {
                return List.of("i18n/custom-module/messages", "i18n/deadman-common/messages");
            }
        }));

        assertThat(basenames).contains("i18n/deadman-common/messages", "i18n/sample-module/messages",
                "i18n/custom-module/messages");
        assertThat(basenames.stream().filter(name -> name.equals("i18n/deadman-common/messages")).count()).isEqualTo(1);
    }

    @Test
    void everyResultCodeHasAllLocales() throws IOException {
        List<String> files = List.of(
                "messages.properties",
                "messages_zh_CN.properties",
                "messages_zh_TW.properties",
                "messages_bo.properties",
                "messages_en.properties");
        for (String file : files) {
            Properties properties = loadCommonBundle(file);
            for (ResultCode code : ResultCode.values()) {
                String value = properties.getProperty(code.messageKey());
                if (ownedByPlugin(code)) {
                    assertThat(value).as(file + " 插件文案不应留在 common：" + code.messageKey()).isNull();
                } else {
                    assertThat(value).as(file + " " + code.messageKey()).isNotBlank();
                }
            }
            assertThat(properties.getProperty("common.param.missing")).contains("{0}");
            assertThat(properties.getProperty("common.upload.multipart_invalid")).isNotBlank();
        }
    }

    @Test
    void shouldResolveResultAndBusinessExceptionByLocale() {
        LocaleContextHolder.setLocale(DeadmanLocales.ENGLISH);
        assertThat(Result.of(ResultCode.USER_NOT_FOUND).getMsg()).isEqualTo("User not found");
        assertThat(new BusinessException(ResultCode.USER_NOT_FOUND).resolveMessage()).isEqualTo("User not found");
        assertThat(new BusinessException(ResultCode.USER_NOT_FOUND, "自定义说明").resolveMessage()).isEqualTo("自定义说明");
        assertThat(MessageSourceHolder.resolve("sample.greeting", "回退")).isEqualTo("Hello");
        assertThat(MessageSourceHolder.resolve("common.param.missing", "缺少必填参数：file", "file"))
                .isEqualTo("Missing required parameter: file");

        LocaleContextHolder.setLocale(DeadmanLocales.TRADITIONAL_CHINESE);
        assertThat(Result.of(ResultCode.USER_NOT_FOUND).getMsg()).isEqualTo("用戶不存在");

        LocaleContextHolder.setLocale(DeadmanLocales.TIBETAN);
        assertThat(Result.of(ResultCode.SUCCESS).getMsg()).isEqualTo("ལེགས་གྲུབ།");
        assertThat(MessageSourceHolder.resolve("sample.greeting", "回退")).isEqualTo("བཀྲ་ཤིས་བདེ་ལེགས།");

        LocaleContextHolder.setLocale(DeadmanLocales.SIMPLIFIED_CHINESE);
        assertThat(Result.of(ResultCode.SUCCESS).getMsg()).isEqualTo("成功");
        assertThat(MessageSourceHolder.resolve("missing.key", "回退")).isEqualTo("回退");
    }

    /**
     * 文案已随插件或扩展模块存放，common 只保留账号、组织与通用错误。
     *
     * @param code 错误码
     * @return 是否由插件或扩展模块提供文案
     */
    private static boolean ownedByPlugin(ResultCode code) {
        String name = code.name();
        return name.startsWith("WECHAT_PAY_") || name.startsWith("WECHAT_BIND_TOKEN_") || name.startsWith("FILE_")
                || name.startsWith("PAY_") || name.startsWith("CRYPTO_") || name.startsWith("LOGISTICS_")
                || name.startsWith("IM_") || name.startsWith("ESS_");
    }

    /**
     * 读取 common 模块的语言文件。
     *
     * @param file 文件名
     * @return 文案项
     */
    private static Properties loadCommonBundle(String file) throws IOException {
        Properties properties = new Properties();
        String path = "/i18n/deadman-common/" + file;
        try (var input = DeadmanI18nTest.class.getResourceAsStream(path)) {
            assertThat(input).as(path).isNotNull();
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
