package com.mtfm.deadman.plugin.ess.tencent.client;

import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ess.v20201111.EssClient;

/**
 * 腾讯电子签 EssClient 工厂。
 * <p>
 * 文件上传需走独立文件服务域名，因此同时提供 API Client 与 File Client。
 */
public class EssClientFactory {

    private final EssTencentPluginProperties properties;

    /**
     * @param properties 插件配置
     */
    public EssClientFactory(EssTencentPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建电子签业务 API 客户端。
     *
     * @return EssClient
     */
    public EssClient createApiClient() {
        return createClient(properties.getEndpoint());
    }

    /**
     * 创建电子签文件服务客户端（UploadFiles 专用）。
     *
     * @return EssClient
     */
    public EssClient createFileClient() {
        return createClient(properties.getFileEndpoint());
    }

    /**
     * 按指定 endpoint 构造 EssClient。
     *
     * @param endpoint 服务域名
     * @return EssClient
     */
    private EssClient createClient(String endpoint) {
        properties.requireProductionConfig();
        Credential credential = new Credential(properties.getSecretId(), properties.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setConnTimeout(properties.getConnTimeoutSeconds());
        httpProfile.setReqMethod("POST");
        httpProfile.setEndpoint(endpoint);

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setSignMethod("TC3-HMAC-SHA256");
        clientProfile.setHttpProfile(httpProfile);
        return new EssClient(credential, properties.getRegion(), clientProfile);
    }
}
