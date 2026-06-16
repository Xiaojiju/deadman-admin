package com.mtfm.deadman.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联 Mapper。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 查询用户绑定的角色编码列表。
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    @Select("""
            SELECT DISTINCT r.role_code
            FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 AND r.status = 1
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 批量查询多个用户的角色编码。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 与角色编码行
     */
    @Select("""
            <script>
            SELECT ur.user_id AS userId, r.role_code AS roleCode
            FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 AND r.status = 1
            WHERE ur.user_id IN
            <foreach collection='userIds' item='userId' open='(' separator=',' close=')'>
              #{userId}
            </foreach>
            ORDER BY ur.user_id ASC, r.role_code ASC
            </script>
            """)
    List<UserRoleCodeRow> selectRoleCodesByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 统计拥有指定角色编码的用户数（去重 user_id）。
     *
     * @param roleCode 角色编码
     * @return 用户数
     */
    @Select("""
            SELECT COUNT(DISTINCT ur.user_id)
            FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0
            WHERE r.role_code = #{roleCode}
            """)
    long countUsersByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 用户角色编码查询行。
     *
     * @param userId   用户 ID
     * @param roleCode 角色编码
     */
    record UserRoleCodeRow(Long userId, String roleCode) {
    }
}
