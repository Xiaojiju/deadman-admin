package com.mtfm.deadman.component.client.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 用户端已认证用户上下文，实现 Spring Security {@link UserDetails} 与统一负载体。
 */
@Getter
public class ClientLoginUser implements UserDetails, ClientAuthenticatedUser {

    /** 用户主键 */
    private final Long userId;
    /** 对外用户编码 */
    private final String userCode;
    /** 主登录标识 */
    private final String loginIdentifier;
    /** 用户昵称 */
    private final String nickname;
    /** 是否启用 */
    private final boolean enabled;
    /** 权限集合（首期无 RBAC，为空） */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 构造用户端登录用户。
     *
     * @param userId          用户 ID
     * @param userCode        用户编码
     * @param loginIdentifier 登录标识
     * @param nickname        昵称
     * @param enabled         是否启用
     * @param authorities     权限集合
     */
    public ClientLoginUser(
            Long userId,
            String userCode,
            String loginIdentifier,
            String nickname,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.userCode = userCode;
        this.loginIdentifier = loginIdentifier;
        this.nickname = nickname;
        this.enabled = enabled;
        this.authorities = authorities == null ? List.of() : List.copyOf(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userCode;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
