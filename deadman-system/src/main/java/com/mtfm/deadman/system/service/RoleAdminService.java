package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.common.spi.PermissionCatalog;
import com.mtfm.deadman.common.spi.UserAuthorityCache;
import com.mtfm.deadman.common.spi.UserRoleAssignment;
import com.mtfm.deadman.common.util.DedupUtils;
import com.mtfm.deadman.system.dto.role.AssignRolePermissionsRequest;
import com.mtfm.deadman.system.dto.role.AssignUserRolesRequest;
import com.mtfm.deadman.system.dto.role.CreateRoleRequest;
import com.mtfm.deadman.system.dto.role.UpdateRoleRequest;
import com.mtfm.deadman.system.entity.SysRole;
import com.mtfm.deadman.system.entity.SysRolePermission;
import com.mtfm.deadman.system.entity.SysUserRole;
import com.mtfm.deadman.system.mapper.SysRolePermissionMapper;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import com.mtfm.deadman.system.vo.role.RoleDetailVO;
import com.mtfm.deadman.system.vo.role.RoleSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 角色与权限分配管理。
 */
@Service
@RequiredArgsConstructor
public class RoleAdminService implements UserRoleAssignment {

    private final SysRoleService sysRoleService;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserAuthorityCache userAuthorityCache;
    private final PermissionCatalog permissionCatalog;
    private final UserBaseService userBaseService;

    /**
     * 是否已有用户绑定 SUPER_ADMIN 角色。
     *
     * @return 是否存在超级管理员用户
     */
    public boolean existsSuperAdminUser() {
        return sysUserRoleMapper.countUsersByRoleCode(SysRoleCodes.SUPER_ADMIN) > 0;
    }

    /**
     * 将用户设为唯一超级管理员（覆盖式，仅保留 SUPER_ADMIN 角色）。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignSuperAdminRole(Long userId) {
        SysRole superAdmin = sysRoleService.requireActiveByCode(SysRoleCodes.SUPER_ADMIN);
        assignUserRoles(userId, new AssignUserRolesRequest(List.of(superAdmin.getId())));
    }

    /**
     * 获取全部角色
     *
     * @return 全部角色
     */
    public List<RoleSummaryVO> listRoles() {
        return sysRoleService.list(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getRoleCode)).stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    public RoleDetailVO getRoleDetail(Long roleId) {
        SysRole role = sysRoleService.requireById(roleId);
        List<String> permissionCodes = resolveRolePermissionCodes(role);
        return new RoleDetailVO(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus(),
                role.getSystemBuiltin() != null && role.getSystemBuiltin() == 1,
                permissionCodes);
    }

    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色详情
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleDetailVO createRole(CreateRoleRequest request) {
        assertCustomRoleCode(request.roleCode());
        if (sysRoleService.findByCode(request.roleCode()) != null) {
            throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
        }
        validatePermissionCodes(request.permissionCodes());
        SysRole role = SysRole.builder()
                .roleCode(request.roleCode())
                .roleName(request.roleName())
                .description(request.description())
                .status(UserStatus.ACTIVE.getValue())
                .systemBuiltin(0)
                .build();
        sysRoleService.save(role);
        replaceRolePermissions(role.getId(), request.permissionCodes());
        userAuthorityCache.evictAllUserAuthorities();
        return getRoleDetail(role.getId());
    }

    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 更新角色请求
     * @return 角色详情
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleDetailVO updateRole(Long roleId, UpdateRoleRequest request) {
        SysRole role = sysRoleService.requireById(roleId);
        if (StringUtils.hasText(request.roleName())) {
            role.setRoleName(request.roleName());
        }
        if (request.description() != null) {
            role.setDescription(request.description());
        }
        if (request.status() != null) {
            if (sysRoleService.isSystemBuiltin(role) && request.status() != UserStatus.ACTIVE.getValue()) {
                throw new BusinessException(ResultCode.ROLE_SYSTEM_PROTECTED);
            }
            role.setStatus(request.status());
        }
        sysRoleService.updateById(role);
        userAuthorityCache.evictAllUserAuthorities();
        return getRoleDetail(roleId);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = sysRoleService.requireById(roleId);
        sysRoleService.assertDeletable(role);
        sysRoleService.removeById(roleId);
        sysRolePermissionMapper
                .delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        userAuthorityCache.evictAllUserAuthorities();
    }

    /**
     * 分配角色权限
     *
     * @param roleId  角色ID
     * @param request 分配角色权限请求
     * @return 角色详情
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleDetailVO assignRolePermissions(Long roleId, AssignRolePermissionsRequest request) {
        SysRole role = sysRoleService.requireById(roleId);
        sysRoleService.assertPermissionAssignable(role);
        validatePermissionCodes(request.permissionCodes());
        replaceRolePermissions(roleId, request.permissionCodes());
        userAuthorityCache.evictAllUserAuthorities();
        return getRoleDetail(roleId);
    }

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param request 分配用户角色请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, AssignUserRolesRequest request) {
        assignUserRoles(userId, request.roleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        userBaseService.requireById(userId);
        assertSuperAdminRolePreserved(userId, roleIds);
        List<SysRole> roles = DedupUtils.dedupeLongs(roleIds).stream()
                .map(sysRoleService::requireById)
                .toList();
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (SysRole role : roles) {
            SysUserRole link = SysUserRole.builder().userId(userId).roleId(role.getId()).build();
            sysUserRoleMapper.insert(link);
        }
        userAuthorityCache.evictUserAuthorities(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllUserRoles(Long userId) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        userAuthorityCache.evictUserAuthorities(userId);
    }

    /**
     * 校验超级管理员角色不可被移除。
     *
     * @param userId  用户 ID
     * @param roleIds 待绑定角色 ID 列表
     */
    private void assertSuperAdminRolePreserved(Long userId, List<Long> roleIds) {
        if (!sysUserRoleMapper.selectRoleCodesByUserId(userId).contains(SysRoleCodes.SUPER_ADMIN)) {
            return;
        }
        SysRole superAdmin = sysRoleService.findByCode(SysRoleCodes.SUPER_ADMIN);
        if (superAdmin == null || roleIds == null || !roleIds.contains(superAdmin.getId())) {
            throw new BusinessException(ResultCode.USER_SUPER_ADMIN_PROTECTED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDefaultUserRole(Long userId) {
        SysRole userRole = sysRoleService.requireActiveByCode(SysRoleCodes.USER);
        long exists = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, userRole.getId()));
        if (exists == 0) {
            sysUserRoleMapper.insert(SysUserRole.builder().userId(userId).roleId(userRole.getId()).build());
        }
        userAuthorityCache.evictUserAuthorities(userId);
    }

    private void replaceRolePermissions(Long roleId, List<String> permissionCodes) {
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        for (String code : DedupUtils.dedupeStrings(permissionCodes)) {
            sysRolePermissionMapper.insert(
                    SysRolePermission.builder().roleId(roleId).permissionCode(code).build());
        }
    }

    private void validatePermissionCodes(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return;
        }
        Set<String> valid = permissionCatalog.allPermissionCodes();
        for (String code : permissionCodes) {
            if (!valid.contains(code)) {
                throw new BusinessException(ResultCode.PERMISSION_INVALID, "无效权限码: " + code);
            }
        }
    }

    private void assertCustomRoleCode(String roleCode) {
        if (SysRoleCodes.SUPER_ADMIN.equals(roleCode) || SysRoleCodes.USER.equals(roleCode)) {
            throw new BusinessException(ResultCode.ROLE_SYSTEM_PROTECTED, "系统保留角色编码不可用于新建");
        }
    }

    private List<String> resolveRolePermissionCodes(SysRole role) {
        if (SysRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            return permissionCatalog.allPermissionCodes().stream().sorted().toList();
        }
        return sysRolePermissionMapper.selectPermissionCodesByRoleId(role.getId());
    }

    private RoleSummaryVO toSummary(SysRole role) {
        return new RoleSummaryVO(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus(),
                role.getSystemBuiltin() != null && role.getSystemBuiltin() == 1);
    }
}
