package com.mtfm.deadman.plugin.datascope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.datascope.entity.UserDataScopeDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 CUSTOM 数据权限可见部门 Mapper。
 */
@Mapper
public interface UserDataScopeDeptMapper extends BaseMapper<UserDataScopeDept> {
}
