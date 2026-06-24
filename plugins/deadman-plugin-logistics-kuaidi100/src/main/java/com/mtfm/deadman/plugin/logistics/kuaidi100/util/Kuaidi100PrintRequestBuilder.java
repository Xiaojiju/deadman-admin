package com.mtfm.deadman.plugin.logistics.kuaidi100.util;

import com.kuaidi100.sdk.request.PrintReq;
import com.kuaidi100.sdk.utils.SignUtils;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.logistics.kuaidi100.config.Kuaidi100LogisticsPluginProperties;

import tools.jackson.databind.json.JsonMapper;

/**
 * 快递100 PrintReq 构建器，统一封装 param 序列化与 printSign 签名。
 */
public class Kuaidi100PrintRequestBuilder {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final Kuaidi100LogisticsPluginProperties properties;

    /**
     * 构造 PrintReq 构建器。
     *
     * @param properties 插件配置
     */
    public Kuaidi100PrintRequestBuilder(Kuaidi100LogisticsPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 构建带签名的 PrintReq。
     *
     * @param method      快递100 method 常量
     * @param paramObject 业务 param 对象（将序列化为 JSON）
     * @return 已签名的 PrintReq
     */
    public PrintReq build(String method, Object paramObject) {
        properties.requireSecret();
        try {
            String param = JSON_MAPPER.writeValueAsString(paramObject);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String sign = SignUtils.printSign(param, timestamp, properties.getKey(), properties.getSecret());
            PrintReq request = new PrintReq();
            request.setMethod(method);
            request.setKey(properties.getKey());
            request.setT(timestamp);
            request.setParam(param);
            request.setSign(sign);
            request.setSecret(properties.getSecret());
            return request;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.LOGISTICS_CONFIG_INVALID, "构建快递100 PrintReq 失败：" + ex.getMessage());
        }
    }
}
