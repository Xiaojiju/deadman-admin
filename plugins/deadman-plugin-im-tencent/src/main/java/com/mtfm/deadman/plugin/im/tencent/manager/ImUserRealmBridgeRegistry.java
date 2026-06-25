package com.mtfm.deadman.plugin.im.tencent.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserRealmBridge;

import lombok.extern.slf4j.Slf4j;

/**
 * IM 用户域桥接注册表，聚合所有 {@link ImUserRealmBridge} 实现。
 */
@Slf4j
@Component
public class ImUserRealmBridgeRegistry {

    private final Map<String, ImUserRealmBridge> bridges;

    /**
     * 构造桥接注册表。
     *
     * @param bridgeList 所有桥接实现
     */
    public ImUserRealmBridgeRegistry(List<ImUserRealmBridge> bridgeList) {
        Map<String, ImUserRealmBridge> registry = new LinkedHashMap<>();
        for (ImUserRealmBridge bridge : bridgeList) {
            String realmId = bridge.realmId();
            if (registry.containsKey(realmId)) {
                log.warn("IM 用户域桥接重复注册，后者覆盖前者：{}", realmId);
            }
            registry.put(realmId, bridge);
        }
        this.bridges = Map.copyOf(registry);
        log.info("IM 用户域桥接注册完成，共 {} 个：{}", bridges.size(), bridges.keySet());
    }

    /**
     * 按用户域标识获取桥接实现。
     *
     * @param realmId 用户域标识
     * @return 桥接实现
     */
    public ImUserRealmBridge require(String realmId) {
        ImUserRealmBridge bridge = bridges.get(realmId);
        if (bridge == null) {
            throw new BusinessException(ResultCode.IM_REALM_UNKNOWN, "IM 用户域未注册：" + realmId);
        }
        return bridge;
    }
}
