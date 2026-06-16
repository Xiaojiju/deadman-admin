package com.mtfm.deadman.security.token;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按 realm 或刷新路径索引 {@link AuthTokenIssueProvider}。
 */
@Component
public class AuthTokenIssueProviderRegistry {

    private final Map<String, AuthTokenIssueProvider> byRealm;
    private final Map<String, AuthTokenIssueProvider> byRefreshPath;

    /**
     * 聚合容器中全部 Provider 实现。
     *
     * @param providers Provider 列表
     */
    public AuthTokenIssueProviderRegistry(List<AuthTokenIssueProvider> providers) {
        this.byRealm = providers.stream()
                .collect(Collectors.toMap(AuthTokenIssueProvider::realm, Function.identity(), (a, b) -> a));
        this.byRefreshPath = providers.stream()
                .collect(Collectors.toMap(AuthTokenIssueProvider::refreshTokenPath, Function.identity(), (a, b) -> a));
    }

    /**
     * 按 realm 获取 Provider。
     *
     * @param realm 端标识
     * @return Provider
     */
    public AuthTokenIssueProvider require(String realm) {
        AuthTokenIssueProvider provider = byRealm.get(realm);
        if (provider == null) {
            throw new IllegalStateException("未注册 AuthTokenIssueProvider，realm=" + realm);
        }
        return provider;
    }

    /**
     * 按刷新路径查找 Provider。
     *
     * @param path 请求路径
     * @return Provider
     */
    public Optional<AuthTokenIssueProvider> findByRefreshPath(String path) {
        if (path == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byRefreshPath.get(path));
    }
}
