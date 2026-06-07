package com.mtfm.deadman.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.system.entity.SysUserPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户职位关联 Mapper。
 */
@Mapper
public interface SysUserPositionMapper extends BaseMapper<SysUserPosition> {

    /**
     * 查询用户绑定的职位 ID 列表。
     */
    @Select("""
            SELECT up.position_id
            FROM sys_user_position up
            INNER JOIN sys_position p ON up.position_id = p.id AND p.is_deleted = 0
            WHERE up.user_id = #{userId}
            ORDER BY up.create_time ASC, up.position_id ASC
            """)
    List<Long> selectPositionIdsByUserId(@Param("userId") Long userId);

    /**
     * 统计绑定指定职位的用户数。
     */
    @Select("""
            SELECT COUNT(DISTINCT up.user_id)
            FROM sys_user_position up
            WHERE up.position_id = #{positionId}
            """)
    long countUsersByPositionId(@Param("positionId") Long positionId);
}
