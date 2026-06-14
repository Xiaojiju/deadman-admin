package com.mtfm.deadman.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.datascope.annotation.DataColumn;
import com.mtfm.deadman.system.entity.UserBase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户基础信息 Mapper。
 */
@Mapper
@DataColumn(dept = "department_id", user = "id")
public interface UserBaseMapper extends BaseMapper<UserBase> {
}
