package com.mtfm.deadman.support.client.file.access;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.support.client.file.config.ClientFileSupportProperties;
import com.mtfm.deadman.support.client.file.spi.SensitiveFileBizTypeContributor;

import lombok.extern.slf4j.Slf4j;

/**
 * 敏感证件类文件的访问规则：识别敏感分类、判定本地静态路径、签发和校验管理端用的限时签名地址。
 */
@Slf4j
@Component
public class SensitiveFilePolicy {

    /** 签名地址里的过期时间参数 */
    public static final String PARAM_EXPIRES = "exp";

    /** 签名地址里的签名参数 */
    public static final String PARAM_SIGNATURE = "sig";

    /** 本地存储键的第一段是业务分类目录，上传时已清洗成这些字符 */
    private static final Pattern SAFE_SEGMENT = Pattern.compile("^[A-Za-z0-9_\\-]+$");

    private final Set<String> sensitive;
    private final String localPrefix;
    private final String publicBaseUrl;
    private final byte[] secret;
    private final long ttlSeconds;

    /**
     * @param contributors 各模块声明的敏感分类
     * @param properties 本模块配置
     * @param environment 读取本地存储前缀和文件插件的 public-base-url
     */
    public SensitiveFilePolicy(ObjectProvider<SensitiveFileBizTypeContributor> contributors,
        ClientFileSupportProperties properties, Environment environment) {
        Set<String> types = new LinkedHashSet<>();
        contributors.orderedStream().forEach(contributor -> contributor.sensitiveBizTypes().forEach(type -> {
            if (StringUtils.hasText(type)) {
                types.add(type.trim().toLowerCase(Locale.ROOT));
            }
        }));
        if (properties.getSensitiveBizTypes() != null) {
            properties.getSensitiveBizTypes().stream().filter(StringUtils::hasText)
                .forEach(type -> types.add(type.trim().toLowerCase(Locale.ROOT)));
        }
        this.sensitive = Collections.unmodifiableSet(types);
        this.localPrefix = normalizePrefix(
            environment.getProperty("deadman.plugin.storage-local.public-url-prefix", "/files"));
        String base = environment.getProperty("deadman.plugin.file.public-base-url");
        this.publicBaseUrl = StringUtils.hasText(base) ? base.trim().replaceAll("/+$", "") : null;
        if (StringUtils.hasText(properties.getSignedUrlSecret())) {
            this.secret = properties.getSignedUrlSecret().getBytes(StandardCharsets.UTF_8);
        } else {
            this.secret = new byte[32];
            new SecureRandom().nextBytes(this.secret);
            log.warn("未配置 deadman.support.client-file.signed-url-secret，敏感文件签名地址使用进程内随机密钥，"
                + "重启后旧地址失效，多实例部署时必须配置");
        }
        this.ttlSeconds = Math.max(60, properties.getSignedUrlTtlSeconds());
        log.info("敏感文件分类：{}，本地静态前缀：{}", this.sensitive, this.localPrefix);
    }

    /**
     * @param bizType 业务分类
     * @return 是否敏感
     */
    public boolean isSensitive(String bizType) {
        return StringUtils.hasText(bizType) && sensitive.contains(bizType.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * @return 全部敏感分类（小写）
     */
    public Set<String> sensitiveBizTypes() {
        return sensitive;
    }

    /**
     * @return 本地存储静态路径前缀，如 /files
     */
    public String localPrefix() {
        return localPrefix;
    }

    /**
     * 判定一个本地静态路径（已解码、不含 context path）。
     *
     * @param path 请求路径
     * @return 判定结果
     */
    public StaticVerdict classifyStaticPath(String path) {
        if (path == null || !path.toLowerCase(Locale.ROOT).startsWith(localPrefix.toLowerCase(Locale.ROOT) + "/")) {
            return StaticVerdict.NOT_LOCAL;
        }
        String rest = path.substring(localPrefix.length() + 1);
        if (!path.startsWith(localPrefix + "/") || rest.isEmpty()) {
            return StaticVerdict.REJECT;
        }
        int slash = rest.indexOf('/');
        String first = slash < 0 ? rest : rest.substring(0, slash);
        // 业务分类目录名只会是字母、数字、下划线、连字符；大小写变体、结尾的点或空格、8.3 短名（~）、
        // 冒号等在 Windows 上可能指向同一个目录，一律拒绝
        if (!SAFE_SEGMENT.matcher(first).matches() || rest.indexOf('\\') >= 0 || rest.indexOf(':') >= 0
            || rest.indexOf('%') >= 0 || rest.contains("//")) {
            return StaticVerdict.REJECT;
        }
        for (String segment : rest.split("/")) {
            if (".".equals(segment) || "..".equals(segment)) {
                return StaticVerdict.REJECT;
            }
        }
        return isSensitive(first) ? StaticVerdict.SENSITIVE : StaticVerdict.PUBLIC;
    }

    /**
     * 文件访问地址如果是敏感分类的本地静态路径，返回路径部分；否则返回 null。
     *
     * @param accessUrl 文件插件给出的地址（相对路径，或按 public-base-url 拼好的绝对地址）
     * @return 本地静态路径
     */
    public String sensitiveLocalPath(String accessUrl) {
        if (!StringUtils.hasText(accessUrl)) {
            return null;
        }
        String url = accessUrl.trim();
        int query = url.indexOf('?');
        if (query >= 0) {
            url = url.substring(0, query);
        }
        if (publicBaseUrl != null && url.startsWith(publicBaseUrl + "/")) {
            url = url.substring(publicBaseUrl.length());
        }
        if (!url.startsWith("/")) {
            return null;
        }
        return classifyStaticPath(url) == StaticVerdict.SENSITIVE ? url : null;
    }

    /**
     * 给管理端用：敏感分类的本地静态地址加上限时签名，其他地址原样返回。
     *
     * @param accessUrl 原地址
     * @return 签名后的地址
     */
    public String signIfSensitive(String accessUrl) {
        String path = sensitiveLocalPath(accessUrl);
        if (path == null) {
            return accessUrl;
        }
        String url = accessUrl.trim();
        int query = url.indexOf('?');
        if (query >= 0) {
            url = url.substring(0, query);
        }
        long expires = Instant.now().getEpochSecond() + ttlSeconds;
        return url + "?" + PARAM_EXPIRES + "=" + expires + "&" + PARAM_SIGNATURE + "=" + signature(path, expires);
    }

    /**
     * 校验静态路径上的签名。
     *
     * @param path 已解码的请求路径
     * @param expires exp 参数
     * @param signature sig 参数
     * @return 是否有效且未过期
     */
    public boolean verify(String path, String expires, String signature) {
        if (!StringUtils.hasText(expires) || !StringUtils.hasText(signature)) {
            return false;
        }
        long exp;
        try {
            exp = Long.parseLong(expires);
        } catch (NumberFormatException ex) {
            return false;
        }
        if (exp < Instant.now().getEpochSecond()) {
            return false;
        }
        return MessageDigest.isEqual(signature(path, exp).getBytes(StandardCharsets.US_ASCII),
            signature.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * 用户端本人查看敏感文件的鉴权地址，需要带 Authorization 头。
     *
     * @param fileId 文件 ID
     * @return 地址
     */
    public String clientContentUrl(Long fileId) {
        return "/client/api/files/" + fileId + "/content";
    }

    private String signature(String path, long expires) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] digest = mac.doFinal((path + "\n" + expires).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HmacSHA256 不可用", ex);
        }
    }

    private static String normalizePrefix(String prefix) {
        String value = StringUtils.hasText(prefix) ? prefix.trim() : "/files";
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * 本地静态路径的判定结果。
     */
    public enum StaticVerdict {
        /** 不是本地存储的静态路径 */
        NOT_LOCAL,
        /** 公开文件，照常访问 */
        PUBLIC,
        /** 敏感文件，必须带有效签名 */
        SENSITIVE,
        /** 路径里有可疑字符，直接拒绝 */
        REJECT
    }
}
