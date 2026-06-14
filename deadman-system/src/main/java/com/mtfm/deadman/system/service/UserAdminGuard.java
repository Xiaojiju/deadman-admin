package com.mtfm.deadman.system.service;

import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 管理端用户操作保护规则（如超级管理员不可停用/删除）。
 */
@Component
@RequiredArgsConstructor
public class UserAdminGuard {

    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 校验目标用户不是超级管理员，否则抛出业务异常。
     *
     * @param userId 用户 ID
     */
    public void assertNotSuperAdminUser(Long userId) {
        if (sysUserRoleMapper.selectRoleCodesByUserId(userId).contains(SysRoleCodes.SUPER_ADMIN)) {
            throw new BusinessException(ResultCode.USER_SUPER_ADMIN_PROTECTED);
        }
    }
}
