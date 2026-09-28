package com.mtfm.deadman.common.spi;

import java.util.List;

/**
 * 模块国际化资源包贡献者。
 * <p>
 * 各模块把文案放在 {@code src/main/resources/i18n/<模块名>/} 后，启动时会被自动扫描注册，无需实现本接口。
 * 资源不在该约定目录时，实现本接口并注册为 Spring Bean。
 * <p>
 * 语言文件：{@code messages.properties}（默认回退，建议与简体中文一致）、{@code messages_zh_CN.properties}、
 * {@code messages_zh_TW.properties}、{@code messages_bo.properties}、{@code messages_en.properties}。
 * 文案键建议带模块前缀。请求通过 {@code Accept-Language} 选择语言。
 */
public interface MessageBasenameContributor {

    /**
     * 本模块资源包基名，不含语言后缀与 {@code .properties}。
     *
     * @return 例如 {@code i18n/deadman-pay/messages}
     */
    List<String> basenames();
}
