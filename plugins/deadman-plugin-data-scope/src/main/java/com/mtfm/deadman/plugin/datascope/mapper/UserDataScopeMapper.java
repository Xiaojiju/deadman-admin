package com.mtfm.deadman.plugin.datascope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.datascope.entity.UserDataScope;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据权限配置 Mapper。
 */
@Mapper
public interface UserDataScopeMapper extends BaseMapper<UserDataScope> {
}
