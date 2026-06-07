package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.system.entity.SysRole;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

/**
 * 系统角色服务
 */
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    /**
     * 根据角色ID获取角色
     * 
     * @param roleId 角色ID
     * @return 角色
     */
    public SysRole requireById(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    /**
     * 根据角色编码获取角色
     * 
     * @param roleCode 角色编码
     * @return 角色
     */
    public SysRole requireByCode(String roleCode) {
        SysRole role = getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    /**
     * 断言角色可删除
     * 
     * @param role 角色
     */
    public void assertDeletable(SysRole role) {
        if (role.getSystemBuiltin() != null && role.getSystemBuiltin() == 1) {
            throw new BusinessException(ResultCode.ROLE_SYSTEM_PROTECTED);
        }
    }

    /**
     * 断言角色可分配权限
     * 
     * @param role 角色
     */
    public void assertPermissionAssignable(SysRole role) {
        if (SysRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            throw new BusinessException(ResultCode.ROLE_SUPER_ADMIN_PROTECTED);
        }
    }

    /**
     * 判断角色是否为系统内置角色
     * 
     * @param role 角色
     * @return 是否为系统内置角色
     */
    public boolean isSystemBuiltin(SysRole role) {
        return role.getSystemBuiltin() != null && role.getSystemBuiltin() == 1;
    }

    /**
     * 根据角色编码获取角色
     * 
     * @param roleCode 角色编码
     * @return 角色
     */
    public SysRole findByCode(String roleCode) {
        return getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
    }

    /**
     * 根据角色编码获取角色，并判断角色是否为启用状态
     * 
     * @param roleCode 角色编码
     * @return 角色
     */
    public SysRole requireActiveByCode(String roleCode) {
        SysRole role = requireByCode(roleCode);
        if (role.getStatus() == null || role.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        return role;
    }
}
