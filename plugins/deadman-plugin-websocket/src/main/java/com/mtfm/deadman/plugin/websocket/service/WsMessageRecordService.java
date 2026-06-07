package com.mtfm.deadman.plugin.websocket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.plugin.websocket.entity.WsMessageRecord;
import com.mtfm.deadman.plugin.websocket.mapper.WsMessageRecordMapper;
import com.mtfm.deadman.plugin.websocket.message.MessageSendStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WebSocket 消息持久化服务。
 */
@Service
public class WsMessageRecordService extends ServiceImpl<WsMessageRecordMapper, WsMessageRecord> {

    /**
     * 查询待重试的消息。
     *
     * @param limit 最大条数
     * @return 待重试记录
     */
    public List<WsMessageRecord> listRetryCandidates(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return list(new LambdaQueryWrapper<WsMessageRecord>()
                .in(WsMessageRecord::getStatus, MessageSendStatus.PENDING.getValue(), MessageSendStatus.RETRYING.getValue())
                .and(w -> w.isNull(WsMessageRecord::getNextRetryTime).or().le(WsMessageRecord::getNextRetryTime, now))
                .orderByAsc(WsMessageRecord::getCreateTime)
                .last("LIMIT " + limit));
    }
}
