package com.mtfm.deadman.security.spi;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth 登录用户解析服务管理器，按 loginGroupId 路由到对应用户体系实现。
 */
@Slf4j
@Component
public class OAuthLoginUserServiceManager {

    private final Map<String, OAuthLoginUserService> services;

    /**
     * 构造 OAuth 用户解析服务管理器。
     *
     * @param serviceList 所有 OAuthLoginUserService Bean
     */
    public OAuthLoginUserServiceManager(List<OAuthLoginUserService> serviceList) {
        Map<String, OAuthLoginUserService> registry = new LinkedHashMap<>();
        for (OAuthLoginUserService service : serviceList) {
            if (registry.containsKey(service.loginGroupId())) {
                log.warn("OAuth 用户解析服务重复注册，后者覆盖前者：{}", service.loginGroupId());
            }
            registry.put(service.loginGroupId(), service);
        }
        this.services = Map.copyOf(registry);
        log.info("OAuth 用户解析服务注册完成，共 {} 个：{}", services.size(), services.keySet());
    }

    /**
     * 按组标识获取 OAuth 用户解析服务。
     *
     * @param loginGroupId 组标识
     * @return 解析服务
     */
    public OAuthLoginUserService require(String loginGroupId) {
        OAuthLoginUserService service = services.get(loginGroupId);
        if (service == null) {
            throw new BusinessException(
                    ResultCode.NOT_FOUND, "OAuth 用户解析服务不存在，loginGroupId=" + loginGroupId);
        }
        return service;
    }
}
