package com.mtfm.deadman.plugin.file.biztype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.file.config.FilePluginProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 已注册文件业务分类注册表，上传时校验 {@code bizType} 是否已登记。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileBizTypeRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private static final Pattern BIZ_TYPE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{0,63}$");

    private final ObjectProvider<FileBizTypeContributor> contributors;
    private final FilePluginProperties filePluginProperties;

    private final Set<String> registeredBizTypes = ConcurrentHashMap.newKeySet();

    private volatile boolean contributorsLoaded;

    /**
     * 注册单个业务分类（供 {@link FileBizTypeRegistrar} 或模块启动逻辑调用）。
     *
     * @param bizType 业务分类标识
     */
    public void register(String bizType) {
        registeredBizTypes.add(normalizeAndValidate(bizType));
    }

    /**
     * 批量注册业务分类。
     *
     * @param bizTypes 业务分类列表
     */
    public void registerAll(Collection<String> bizTypes) {
        if (bizTypes == null) {
            return;
        }
        bizTypes.forEach(this::register);
    }

    /**
     * 校验业务分类是否允许用于上传。
     *
     * @param bizType 业务分类
     */
    public void requireRegistered(String bizType) {
        if (!filePluginProperties.isBizTypeStrict()) {
            normalizeAndValidate(bizType);
            return;
        }
        String normalized = normalizeAndValidate(bizType);
        if (!registeredBizTypes.contains(normalized)) {
            throw new BusinessException(ResultCode.FILE_BIZ_TYPE_UNREGISTERED, "业务分类未注册: " + normalized);
        }
    }

    /**
     * 列出当前已注册的业务分类（只读快照）。
     *
     * @return 业务分类列表
     */
    public List<String> listRegistered() {
        List<String> snapshot = new ArrayList<>(registeredBizTypes);
        Collections.sort(snapshot);
        return Collections.unmodifiableList(snapshot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        if (contributorsLoaded) {
            return;
        }
        contributorsLoaded = true;
        contributors.forEach(contributor -> registerAll(contributor.contribute()));
        log.info("文件业务分类注册完成，共 {} 个：{}", registeredBizTypes.size(), listRegistered());
    }

    private static String normalizeAndValidate(String bizType) {
        if (!StringUtils.hasText(bizType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "业务分类不能为空");
        }
        String normalized = bizType.trim();
        if (!BIZ_TYPE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(
                    ResultCode.BAD_REQUEST,
                    "业务分类格式无效，仅允许字母、数字、下划线与连字符，且以字母开头");
        }
        return normalized;
    }
}
