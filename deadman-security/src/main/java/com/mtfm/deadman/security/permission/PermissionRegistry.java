package com.mtfm.deadman.security.permission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 运行时权限注册表，聚合所有 {@link PermissionContributor} 贡献的权限定义。
 */
@Slf4j
@Component
public class PermissionRegistry implements PermissionCatalog, SmartInitializingSingleton {

    private final ObjectProvider<PermissionContributor> contributors;
    private final List<PermissionGroupDescriptor> groups = new CopyOnWriteArrayList<>();
    private final Map<String, PermissionItemDescriptor> permissionByCode = new LinkedHashMap<>();

    public PermissionRegistry(ObjectProvider<PermissionContributor> contributors) {
        this.contributors = contributors;
    }

    /**
     * 所有单例初始化完成后聚合权限定义。
     */
    @Override
    public void afterSingletonsInstantiated() {
        contributors.forEach(contributor -> registerGroups(contributor.contribute()));
        groups.sort(Comparator.comparing(PermissionGroupDescriptor::code));
        log.info("权限注册完成，共 {} 个功能集、{} 个权限码", groups.size(), permissionByCode.size());
    }

    @Override
    public Set<String> allPermissionCodes() {
        return Set.copyOf(permissionByCode.keySet());
    }

    @Override
    public boolean isValidPermissionCode(String code) {
        return StringUtils.hasText(code) && permissionByCode.containsKey(code);
    }

    @Override
    public List<PermissionGroupDescriptor> listPermissionGroups() {
        return List.copyOf(groups);
    }

    @Override
    public List<PermissionItemDescriptor> listAllPermissionItems() {
        return List.copyOf(permissionByCode.values());
    }

    private void registerGroups(List<PermissionGroupDescriptor> contributedGroups) {
        if (contributedGroups == null || contributedGroups.isEmpty()) {
            return;
        }
        for (PermissionGroupDescriptor group : contributedGroups) {
            if (group == null || !StringUtils.hasText(group.code())) {
                log.warn("忽略无效权限组: {}", group);
                continue;
            }
            List<PermissionItemDescriptor> items = normalizeItems(group);
            boolean duplicatedGroup = groups.stream().anyMatch(existing -> existing.code().equals(group.code()));
            if (duplicatedGroup) {
                log.warn("权限组编码重复，跳过后者: {}", group.code());
                continue;
            }
            groups.add(new PermissionGroupDescriptor(group.code(), group.label(), items));
            for (PermissionItemDescriptor item : items) {
                if (permissionByCode.containsKey(item.code())) {
                    log.warn("权限码重复，跳过后者: {}", item.code());
                    continue;
                }
                permissionByCode.put(item.code(), item);
            }
        }
    }

    private List<PermissionItemDescriptor> normalizeItems(PermissionGroupDescriptor group) {
        if (group.permissions() == null || group.permissions().isEmpty()) {
            return List.of();
        }
        List<PermissionItemDescriptor> items = new ArrayList<>();
        for (PermissionItemDescriptor item : group.permissions()) {
            if (item == null || !StringUtils.hasText(item.code())) {
                log.warn("忽略无效权限项，group={}", group.code());
                continue;
            }
            items.add(item);
        }
        return List.copyOf(items);
    }
}
