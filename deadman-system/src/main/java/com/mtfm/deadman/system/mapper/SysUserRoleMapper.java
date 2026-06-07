package com.mtfm.deadman.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("""
            SELECT DISTINCT r.role_code
            FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 AND r.status = 1
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 统计拥有指定角色编码的用户数（去重 user_id）。
     */
    @Select("""
            SELECT COUNT(DISTINCT ur.user_id)
            FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0
            WHERE r.role_code = #{roleCode}
            """)
    long countUsersByRoleCode(@Param("roleCode") String roleCode);
}
