package com.mtfm.deadman.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.system.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    @Select("""
            SELECT DISTINCT rp.permission_code
            FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 AND r.status = 1
            INNER JOIN sys_role_permission rp ON rp.role_id = r.id
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT permission_code
            FROM sys_role_permission
            WHERE role_id = #{roleId}
            """)
    List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);
}
