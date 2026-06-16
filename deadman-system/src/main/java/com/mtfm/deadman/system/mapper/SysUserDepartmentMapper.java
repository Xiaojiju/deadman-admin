package com.mtfm.deadman.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.system.entity.SysUserDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户部门关联 Mapper。
 */
@Mapper
public interface SysUserDepartmentMapper extends BaseMapper<SysUserDepartment> {

    /**
     * 查询用户绑定的部门 ID 列表（主部门优先）。
     *
     * @param userId 用户 ID
     * @return 部门 ID 列表
     */
    @Select("""
            SELECT ud.dept_id
            FROM sys_user_department ud
            INNER JOIN sys_department d ON ud.dept_id = d.id AND d.is_deleted = 0
            WHERE ud.user_id = #{userId}
            ORDER BY ud.is_primary DESC, ud.create_time ASC, ud.dept_id ASC
            """)
    List<Long> selectDeptIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户主部门 ID。
     *
     * @param userId 用户 ID
     * @return 主部门 ID，未设置时返回 null
     */
    @Select("""
            SELECT ud.dept_id
            FROM sys_user_department ud
            INNER JOIN sys_department d ON ud.dept_id = d.id AND d.is_deleted = 0
            WHERE ud.user_id = #{userId} AND ud.is_primary = 1
            LIMIT 1
            """)
    Long selectPrimaryDeptIdByUserId(@Param("userId") Long userId);

    /**
     * 查询部门下的用户 ID 列表。
     *
     * @param deptId 部门 ID
     * @return 用户 ID 列表
     */
    @Select("""
            SELECT DISTINCT ud.user_id
            FROM sys_user_department ud
            INNER JOIN user_base u ON ud.user_id = u.id AND u.is_deleted = 0
            WHERE ud.dept_id = #{deptId}
            ORDER BY ud.user_id ASC
            """)
    List<Long> selectUserIdsByDeptId(@Param("deptId") Long deptId);

    /**
     * 统计部门下关联的用户数。
     *
     * @param deptId 部门 ID
     * @return 用户数
     */
    @Select("""
            SELECT COUNT(DISTINCT ud.user_id)
            FROM sys_user_department ud
            INNER JOIN user_base u ON ud.user_id = u.id AND u.is_deleted = 0
            WHERE ud.dept_id = #{deptId}
            """)
    long countUsersByDeptId(@Param("deptId") Long deptId);

    /**
     * 批量查询多个用户的部门 ID 列表。
     *
     * @param userIds 用户 ID 列表
     * @return 用户部门关联记录
     */
    @Select("""
            <script>
            SELECT ud.user_id, ud.dept_id, ud.is_primary
            FROM sys_user_department ud
            INNER JOIN sys_department d ON ud.dept_id = d.id AND d.is_deleted = 0
            WHERE ud.user_id IN
            <foreach collection='userIds' item='userId' open='(' separator=',' close=')'>
              #{userId}
            </foreach>
            ORDER BY ud.user_id ASC, ud.is_primary DESC, ud.create_time ASC
            </script>
            """)
    List<SysUserDepartment> selectByUserIds(@Param("userIds") List<Long> userIds);
}
