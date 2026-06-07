package com.mtfm.deadman.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 用户权限快照，用于 Redis 缓存。
 * <p>
 * 用户绑定多角色时，角色编码与权限码均会去重合并。
 * 
 * @param roleCodes       角色编码
 * @param permissionCodes 权限码
 */
public record UserAuthoritySnapshot(Set<String> roleCodes, Set<String> permissionCodes) implements Serializable {

    /**
     * 转换为 Spring Security 的 GrantedAuthority 列表
     * 
     * @return GrantedAuthority 列表
     */
    public Collection<? extends GrantedAuthority> toGrantedAuthorities() {
        List<SimpleGrantedAuthority> roleAuthorities = roleCodes.stream()
                .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                .toList();
        List<SimpleGrantedAuthority> permissionAuthorities = permissionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return java.util.stream.Stream.concat(roleAuthorities.stream(), permissionAuthorities.stream()).toList();
    }

    /**
     * 是否有角色
     * 
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    public boolean hasRole(String roleCode) {
        return roleCodes.contains(roleCode);
    }

    /**
     * 是否有权限
     * 
     * @param permissionCode 权限码
     * @return 是否有权限
     */
    public boolean hasPermission(String permissionCode) {
        return permissionCodes.contains(permissionCode);
    }
}
