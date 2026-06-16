package com.mtfm.deadman.security.authentication.provider;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 登录 Provider 组管理器，聚合各用户体系的 Provider 组与对应 AuthenticationManager。
 */
@Slf4j
@Component
public class LoginProviderGroupManager {

    private final Map<String, LoginProviderGroup> groups;
    private final Map<String, List<LoginProvider>> providersByGroup;
    private final Map<String, AuthenticationManager> authenticationManagers;

    /**
     * 构造 Provider 组管理器。
     *
     * @param groupContributors 组贡献者列表
     * @param loginProviders    所有 LoginProvider Bean
     */
    public LoginProviderGroupManager(
            List<LoginProviderGroupContributor> groupContributors, List<LoginProvider> loginProviders) {
        Map<String, LoginProviderGroup> groupRegistry = new LinkedHashMap<>();
        for (LoginProviderGroupContributor contributor : groupContributors) {
            LoginProviderGroup group = contributor.group();
            if (groupRegistry.containsKey(group.groupId())) {
                log.warn("登录 Provider 组重复注册，后者覆盖前者：{}", group.groupId());
            }
            groupRegistry.put(group.groupId(), group);
        }
        this.groups = Map.copyOf(groupRegistry);

        Map<String, List<LoginProvider>> providerRegistry = new LinkedHashMap<>();
        for (LoginProvider provider : loginProviders) {
            providerRegistry.computeIfAbsent(provider.loginGroupId(), ignored -> new ArrayList<>()).add(provider);
        }
        this.providersByGroup = Map.copyOf(providerRegistry);

        Map<String, AuthenticationManager> managerRegistry = new LinkedHashMap<>();
        for (Map.Entry<String, List<LoginProvider>> entry : providerRegistry.entrySet()) {
            List<AuthenticationProvider> springProviders = entry.getValue().stream()
                    .<AuthenticationProvider>map(LoginProviderAuthenticationProvider::new)
                    .toList();
            managerRegistry.put(entry.getKey(), new ProviderManager(springProviders));
        }
        this.authenticationManagers = Map.copyOf(managerRegistry);

        log.info(
                "登录 Provider 组注册完成，共 {} 个组：{}",
                groups.size(),
                groups.keySet());
        providersByGroup.forEach((groupId, providers) -> log.info(
                "  组 {} 含 {} 个 Provider：{}",
                groupId,
                providers.size(),
                providers.stream().map(LoginProvider::providerId).toList()));
    }

    /**
     * 获取指定组的 Provider 组描述。
     *
     * @param groupId 组标识
     * @return Provider 组
     */
    public LoginProviderGroup requireGroup(String groupId) {
        LoginProviderGroup group = groups.get(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "登录 Provider 组不存在：" + groupId);
        }
        return group;
    }

    /**
     * 按 API 前缀查找 Provider 组。
     *
     * @param apiPrefix API 前缀
     * @return Provider 组
     */
    public Optional<LoginProviderGroup> findGroupByApiPrefix(String apiPrefix) {
        if (apiPrefix == null || apiPrefix.isBlank()) {
            return Optional.empty();
        }
        return groups.values().stream()
                .filter(group -> apiPrefix.startsWith(group.apiPrefix()))
                .findFirst();
    }

    /**
     * 列出指定组下所有 Provider。
     *
     * @param groupId 组标识
     * @return Provider 列表
     */
    public List<LoginProvider> listProviders(String groupId) {
        return List.copyOf(providersByGroup.getOrDefault(groupId, List.of()));
    }

    /**
     * 获取指定组的 AuthenticationManager。
     *
     * @param groupId 组标识
     * @return 认证管理器
     */
    public AuthenticationManager requireAuthenticationManager(String groupId) {
        AuthenticationManager manager = authenticationManagers.get(groupId);
        if (manager == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "登录 Provider 组不存在或未注册 Provider：" + groupId);
        }
        return manager;
    }

    /**
     * 根据登录请求 URI 解析 Provider ID。
     *
     * @param groupId  组标识
     * @param loginUri 登录请求 URI
     * @return Provider ID，未匹配时返回 unknown
     */
    public String resolveProviderIdByLoginUri(String groupId, String loginUri) {
        if (loginUri == null || loginUri.isBlank()) {
            return "unknown";
        }
        LoginProviderGroup group = requireGroup(groupId);
        for (LoginProvider provider : listProviders(groupId)) {
            if (loginUri.equals(provider.resolveLoginEndpoint(group))) {
                return provider.providerId();
            }
        }
        return "unknown";
    }
}
