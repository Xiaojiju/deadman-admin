package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.system.entity.SysDictItem;
import com.mtfm.deadman.system.mapper.SysDictItemMapper;
import org.springframework.stereotype.Service;
import com.mtfm.deadman.system.support.SystemErrorCodes;
import com.mtfm.deadman.system.support.SystemMessages;

/**
 * 字典项基础数据服务。
 */
@Service
public class SysDictItemService extends ServiceImpl<SysDictItemMapper, SysDictItem> {

    /**
     * 按主键读取字典项。
     *
     * @param itemId 字典项 ID
     * @return 字典项
     */
    public SysDictItem requireById(Long itemId) {
        SysDictItem item = getById(itemId);
        if (item == null) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_NOT_FOUND);
        }
        return item;
    }
}
