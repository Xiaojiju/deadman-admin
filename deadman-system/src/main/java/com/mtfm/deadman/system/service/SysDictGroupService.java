package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.system.entity.SysDictGroup;
import com.mtfm.deadman.system.mapper.SysDictGroupMapper;
import org.springframework.stereotype.Service;
import com.mtfm.deadman.system.support.SystemErrorCodes;
import com.mtfm.deadman.system.support.SystemMessages;

/**
 * 字典组基础数据服务。
 */
@Service
public class SysDictGroupService extends ServiceImpl<SysDictGroupMapper, SysDictGroup> {

    /**
     * 按主键读取字典组。
     *
     * @param groupId 字典组 ID
     * @return 字典组
     */
    public SysDictGroup requireById(Long groupId) {
        SysDictGroup group = getById(groupId);
        if (group == null) {
            throw SystemMessages.of(SystemErrorCodes.DICT_GROUP_NOT_FOUND);
        }
        return group;
    }

    /**
     * 按编码读取字典组。
     *
     * @param groupCode 字典组编码
     * @return 字典组
     */
    public SysDictGroup requireByCode(String groupCode) {
        SysDictGroup group = getOne(new LambdaQueryWrapper<SysDictGroup>().eq(SysDictGroup::getGroupCode, groupCode));
        if (group == null) {
            throw SystemMessages.of(SystemErrorCodes.DICT_GROUP_NOT_FOUND);
        }
        return group;
    }
}
