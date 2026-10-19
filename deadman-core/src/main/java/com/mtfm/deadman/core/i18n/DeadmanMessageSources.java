package com.mtfm.deadman.core.i18n;

import com.mtfm.deadman.common.i18n.DeadmanLocales;
import com.mtfm.deadman.common.i18n.MessageSourceHolder;
import com.mtfm.deadman.common.spi.MessageBasenameContributor;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 汇总各模块资源包基名，并创建 UTF-8 的 {@link ResourceBundleMessageSource}。
 */
public final class DeadmanMessageSources {

    private DeadmanMessageSources() {
    }

    /**
     * 合并 classpath 扫描结果与各模块显式贡献的基名，保持先扫描、后贡献的顺序。同名基名只保留一次。
     *
     * @param contributors 显式贡献者，可为空
     * @return 去重后的基名
     */
    public static List<String> collectBasenames(List<MessageBasenameContributor> contributors) {
        LinkedHashSet<String> basenames = new LinkedHashSet<>(MessageBasenameScanner.scan());
        if (contributors == null) {
            return List.copyOf(basenames);
        }
        for (MessageBasenameContributor contributor : contributors) {
            if (contributor == null || contributor.basenames() == null) {
                continue;
            }
            basenames.addAll(contributor.basenames());
        }
        return List.copyOf(basenames);
    }

    /**
     * 创建文案源并绑定到 {@link MessageSourceHolder}。找不到键时不把键本身当文案，交给调用方的默认文案。
     *
     * @param basenames 资源包基名
     * @return 已绑定的文案源
     */
    public static ResourceBundleMessageSource create(List<String> basenames) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        if (basenames != null && !basenames.isEmpty()) {
            messageSource.setBasenames(basenames.toArray(String[]::new));
        }
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setDefaultLocale(DeadmanLocales.DEFAULT);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(false);
        MessageSourceHolder.bind(messageSource);
        return messageSource;
    }
}
