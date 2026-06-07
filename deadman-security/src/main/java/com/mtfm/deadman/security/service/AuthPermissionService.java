package com.mtfm.deadman.security.service;

import com.mtfm.deadman.common.constants.CacheNames;
import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.security.permission.PermissionCatalog;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.mapper.SysRolePermissionMapper;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.common.util.DedupUtils;
import com.mtfm.deadman.common.spi.UserAuthorityCache;
import com.mtfm.deadman.security.vo.auth.UserAuthorityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 解析用户角色与权限码，构建 {@link LoginUser} 及权限视图。
 * <p>
 * 用户绑定多角色时，角色编码与权限码均会去重合并。
 */
@Service
@RequiredArgsConstructor
public class AuthPermissionService implements UserAuthorityCache {

    private static final String ROLE_PREFIX = "ROLE_";

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final PermissionCatalog permissionCatalog;

    /**
     * 构建登录用户
     * 
     * @param userBase 用户基础信息
     * @return 登录用户
     */
    @Cacheable(value = CacheNames.USER_AUTHORITIES, key = "#userBase.id")
    public LoginUser buildLoginUser(UserBase userBase) {
        boolean enabled = userBase.getStatus() != null && userBase.getStatus() == UserStatus.ACTIVE.getValue();
        Set<String> roleCodes = loadRoleCodes(userBase.getId());
        Set<String> permissionCodes = resolvePermissionCodesForAuthorities(userBase.getId(), roleCodes);
        return new LoginUser(
                userBase.getId(),
                userBase.getUserCode(),
                userBase.getUserCode(),
                "",
                enabled,
                roleCodes,
                buildAuthorities(roleCodes, permissionCodes));
    }

    /**
     * 获取用户权限
     * 
     * @param userId 用户ID
     * @return 用户权限
     */
    public UserAuthorityVO getUserAuthority(Long userId) {
        Set<String> roleCodes = loadRoleCodes(userId);
        Set<String> permissionCodes = resolvePermissionCodesForDisplay(userId, roleCodes);
        return new UserAuthorityVO(
                roleCodes.stream().sorted().toList(),
                permissionCodes.stream().sorted().toList(),
                roleCodes.contains(SysRoleCodes.SUPER_ADMIN));
    }

    /**
     * 清除用户权限缓存
     * 
     * @param userId 用户ID
     */
    @CacheEvict(value = CacheNames.USER_AUTHORITIES, key = "#userId")
    public void evictUserAuthorities(Long userId) {
    }

    /**
     * 清除所有用户权限缓存
     */
    @CacheEvict(value = CacheNames.USER_AUTHORITIES, allEntries = true)
    public void evictAllUserAuthorities() {
    }

    /**
     * 加载用户角色编码
     * 
     * @param userId 用户ID
     * @return 用户角色编码
     */
    private Set<String> loadRoleCodes(Long userId) {
        return DedupUtils.dedupeStrings(sysUserRoleMapper.selectRoleCodesByUserId(userId));
    }

    /**
     * 构建 Spring Security 权限：超级管理员仅保留角色，具体权限码由方法安全表达式按角色放行。
     */
    private Set<String> resolvePermissionCodesForAuthorities(Long userId, Set<String> roleCodes) {
        if (roleCodes.contains(SysRoleCodes.SUPER_ADMIN)) {
            return Set.of();
        }
        return DedupUtils.dedupeStrings(sysRolePermissionMapper.selectPermissionCodesByUserId(userId));
    }

    /**
     * 权限清单 API：超级管理员返回已注册的全部权限码（供前端展示）。
     */
    private Set<String> resolvePermissionCodesForDisplay(Long userId, Set<String> roleCodes) {
        if (roleCodes.contains(SysRoleCodes.SUPER_ADMIN)) {
            return permissionCatalog.allPermissionCodes();
        }
        return DedupUtils.dedupeStrings(sysRolePermissionMapper.selectPermissionCodesByUserId(userId));
    }

    /**
     * 构建用户权限
     * 
     * @param roleCodes       用户角色编码
     * @param permissionCodes 用户权限码
     * @return 用户权限
     */
    private List<GrantedAuthority> buildAuthorities(Set<String> roleCodes, Set<String> permissionCodes) {
        Set<String> authorityKeys = new LinkedHashSet<>();
        roleCodes.forEach(code -> authorityKeys.add(ROLE_PREFIX + code));
        authorityKeys.addAll(permissionCodes);
        List<GrantedAuthority> authorities = new ArrayList<>(authorityKeys.size());
        authorityKeys.forEach(key -> authorities.add(new SimpleGrantedAuthority(key)));
        return authorities;
    }
}
