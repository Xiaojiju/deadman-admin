package com.mtfm.deadman.security;

import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.spi.DataScopeAuthPrincipal;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 已认证用户上下文，username 字段存放 userCode（非数据库主键）。
 * <p>
 * 用户绑定多角色时，角色编码与权限码均会去重合并。
 */
@Getter
public class LoginUser implements UserDetails, DataScopeAuthPrincipal {

    /** 用户ID */
    private final Long userId;
    /** 用户编码 */
    private final String userCode;
    /** 用户名 */
    private final String username;
    /**
     * 密码
     * 为了兼容 Spring Security 的 UserDetails 接口，必须存在，但实际不使用。
     * 
     * @see #getPassword()
     */
    private final String password;
    /** 是否启用 */
    private final boolean enabled;
    /** 角色编码 */
    private final Set<String> roleCodes;
    /** 权限 */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 创建已认证用户上下文
     * 
     * @param userId      用户ID
     * @param userCode    用户编码
     * @param username    用户名
     * @param password    密码
     * @param enabled     是否启用
     * @param roleCodes   角色编码
     * @param authorities 权限
     */
    public LoginUser(
            Long userId,
            String userCode,
            String username,
            String password,
            boolean enabled,
            Set<String> roleCodes,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.userCode = userCode;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.roleCodes = roleCodes == null ? Set.of() : Set.copyOf(roleCodes);
        this.authorities = authorities == null ? List.of() : List.copyOf(authorities);
    }

    /**
     * 是否超级管理员
     * 
     * @return 是否超级管理员
     */
    public boolean isSuperAdmin() {
        return roleCodes.contains(SysRoleCodes.SUPER_ADMIN);
    }

    @Override
    public boolean superAdmin() {
        return isSuperAdmin();
    }

    @Override
    public Long userId() {
        return userId;
    }

    /**
     * 是否有权限
     * 
     * @param permissionCode 权限码
     * @return 是否有权限
     */
    public boolean hasPermission(String permissionCode) {
        if (isSuperAdmin()) {
            return true;
        }
        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(permissionCode::equals);
    }

    /**
     * 获取权限
     * 
     * @return 权限
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 获取密码
     * 
     * @return 密码
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账号是否过期
     * 
     * @return 账号是否过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否锁定
     * 
     * @return 账号是否锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否过期
     * 
     * @return 凭证是否过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否启用
     * 
     * @return 是否启用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
