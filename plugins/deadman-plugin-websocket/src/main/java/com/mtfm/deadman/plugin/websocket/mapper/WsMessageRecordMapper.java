package com.mtfm.deadman.plugin.websocket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.websocket.entity.WsMessageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * WebSocket 消息持久化 Mapper。
 */
@Mapper
public interface WsMessageRecordMapper extends BaseMapper<WsMessageRecord> {
}
