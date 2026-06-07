package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.entity.SysDepartment;
import com.mtfm.deadman.system.mapper.SysDepartmentMapper;
import org.springframework.stereotype.Service;

/**
 * 部门基础数据服务。
 */
@Service
public class SysDepartmentService extends ServiceImpl<SysDepartmentMapper, SysDepartment> {

    /**
     * 根据 ID 获取部门，不存在时抛业务异常。
     *
     * @param departmentId 部门 ID
     * @return 部门实体
     */
    public SysDepartment requireById(Long departmentId) {
        SysDepartment department = getById(departmentId);
        if (department == null) {
            throw new BusinessException(ResultCode.DEPARTMENT_NOT_FOUND);
        }
        return department;
    }
}
