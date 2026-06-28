package com.mtfm.deadman.component.openauth.manager;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.openauth.spi.OpenAuthRealmContributor;
import com.mtfm.deadman.component.openauth.spi.OpenAuthScopeResolver;
import com.mtfm.deadman.component.openauth.spi.OpenAuthSubject;
import com.mtfm.deadman.component.openauth.spi.OpenAuthSubjectResolver;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 开放授权 SPI 注册表，聚合 realm、主体解析与 scope 解析实现。
 */
@Component
public class OpenAuthSpiRegistry {

    private final Map<String, OpenAuthRealmContributor> realmContributors;
    private final Map<String, OpenAuthSubjectResolver> subjectResolvers;
    private final Map<String, OpenAuthScopeResolver> scopeResolvers;

    /**
     * @param realmContributors  用户域贡献者列表
     * @param subjectResolvers   主体解析器列表
     * @param scopeResolvers     scope 解析器列表
     */
    public OpenAuthSpiRegistry(
            List<OpenAuthRealmContributor> realmContributors,
            List<OpenAuthSubjectResolver> subjectResolvers,
            List<OpenAuthScopeResolver> scopeResolvers) {
        this.realmContributors = indexRealms(realmContributors);
        this.subjectResolvers = indexByRealm(subjectResolvers, OpenAuthSubjectResolver::realmId);
        this.scopeResolvers = indexByRealm(scopeResolvers, OpenAuthScopeResolver::realmId);
    }

    /**
     * 获取用户域贡献者。
     *
     * @param realmId 域标识
     * @return 贡献者
     */
    public OpenAuthRealmContributor requireRealm(String realmId) {
        OpenAuthRealmContributor contributor = realmContributors.get(realmId);
        if (contributor == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "开放授权用户域未注册: " + realmId);
        }
        return contributor;
    }

    /**
     * 解析当前认证主体。
     *
     * @param realmId        域标识
     * @param authentication 认证对象
     * @return 授权主体
     */
    public OpenAuthSubject resolveSubject(String realmId, Authentication authentication) {
        OpenAuthSubjectResolver resolver = Optional.ofNullable(subjectResolvers.get(realmId))
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "开放授权主体解析器未配置: " + realmId));
        if (!resolver.supports(authentication)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "当前登录态与开放授权用户域不匹配");
        }
        return resolver.resolve(authentication);
    }

    /**
     * 获取 scope 解析器。
     *
     * @param realmId 域标识
     * @return scope 解析器
     */
    public OpenAuthScopeResolver requireScopeResolver(String realmId) {
        return Optional.ofNullable(scopeResolvers.get(realmId))
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "开放授权 scope 解析器未配置: " + realmId));
    }

    private static <T> Map<String, T> indexByRealm(List<T> items, java.util.function.Function<T, String> realmExtractor) {
        Map<String, T> registry = new LinkedHashMap<>();
        for (T item : items) {
            registry.put(realmExtractor.apply(item), item);
        }
        return Map.copyOf(registry);
    }

    private static Map<String, OpenAuthRealmContributor> indexRealms(List<OpenAuthRealmContributor> contributors) {
        Map<String, OpenAuthRealmContributor> registry = new LinkedHashMap<>();
        for (OpenAuthRealmContributor contributor : contributors) {
            registry.put(contributor.realmId(), contributor);
        }
        return Map.copyOf(registry);
    }
}
