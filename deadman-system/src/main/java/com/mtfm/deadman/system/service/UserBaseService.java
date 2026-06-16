package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.mapper.UserBaseMapper;
import org.springframework.stereotype.Service;

/**
 * 用户基础信息服务
 */
@Service
public class UserBaseService extends ServiceImpl<UserBaseMapper, UserBase> {

    /**
     * 根据用户ID获取用户基础信息
     * 
     * @param userId 用户ID
     * @return 用户基础信息
     */
    public UserBase requireById(Long userId) {
        UserBase user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 统计指定 ID 列表中存在的用户数。
     *
     * @param userIds 用户 ID 列表
     * @return 存在的用户数
     */
    public long countByIds(java.util.List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return lambdaQuery().in(UserBase::getId, userIds).count();
    }
}
