package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.entity.SysPosition;
import com.mtfm.deadman.system.mapper.SysPositionMapper;
import org.springframework.stereotype.Service;

/**
 * 职位基础数据服务。
 */
@Service
public class SysPositionService extends ServiceImpl<SysPositionMapper, SysPosition> {

    /**
     * 根据 ID 获取职位，不存在时抛业务异常。
     *
     * @param positionId 职位 ID
     * @return 职位实体
     */
    public SysPosition requireById(Long positionId) {
        SysPosition position = getById(positionId);
        if (position == null) {
            throw new BusinessException(ResultCode.POSITION_NOT_FOUND);
        }
        return position;
    }
}
