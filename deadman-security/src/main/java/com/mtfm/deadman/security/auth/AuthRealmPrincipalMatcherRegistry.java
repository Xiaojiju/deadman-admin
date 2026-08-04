package com.mtfm.deadman.security.auth;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.spi.AuthRealmPrincipalMatcher;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 认证域 Principal 匹配器注册表。
 */
@Component
public class AuthRealmPrincipalMatcherRegistry {

    private final Map<AuthRealm, AuthRealmPrincipalMatcher> matchers;

    /**
     * 汇总各模块注册的匹配器。
     *
     * @param matcherList SPI 实现列表
     */
    public AuthRealmPrincipalMatcherRegistry(List<AuthRealmPrincipalMatcher> matcherList) {
        Map<AuthRealm, AuthRealmPrincipalMatcher> map = new EnumMap<>(AuthRealm.class);
        for (AuthRealmPrincipalMatcher matcher : matcherList) {
            AuthRealmPrincipalMatcher previous = map.putIfAbsent(matcher.realm(), matcher);
            if (previous != null) {
                throw new IllegalStateException("AuthRealm 重复注册: " + matcher.realm());
            }
        }
        this.matchers = Map.copyOf(map);
    }

    /**
     * 判断 principal 是否匹配指定认证域。
     *
     * @param realm     认证域
     * @param principal 当前 principal
     * @return 匹配时返回 true
     */
    public boolean matches(AuthRealm realm, Object principal) {
        AuthRealmPrincipalMatcher matcher = matchers.get(realm);
        if (matcher == null) {
            throw new IllegalStateException("未注册 AuthRealm 匹配器: " + realm);
        }
        return matcher.matches(principal);
    }
}
