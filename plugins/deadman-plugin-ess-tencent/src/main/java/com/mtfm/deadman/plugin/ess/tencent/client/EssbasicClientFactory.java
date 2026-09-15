package com.mtfm.deadman.plugin.ess.tencent.client;

import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.essbasic.v20210526.EssbasicClient;

/**
 * 腾讯电子签渠道版（essbasic）EssbasicClient 工厂。
 */
public class EssbasicClientFactory {

    private final EssTencentPluginProperties properties;

    /**
     * @param properties 插件配置
     */
    public EssbasicClientFactory(EssTencentPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建渠道版电子签 API 客户端。
     *
     * @return EssbasicClient
     */
    public EssbasicClient createApiClient() {
        return createClient(properties.resolveChannelEndpoint());
    }

    /**
     * 创建渠道版文件服务客户端（UploadFiles 专用）。
     *
     * @return EssbasicClient
     */
    public EssbasicClient createFileClient() {
        return createClient(properties.getFileEndpoint());
    }

    /**
     * 按指定 endpoint 构造渠道版客户端。
     *
     * @param endpoint 服务域名
     * @return EssbasicClient
     */
    private EssbasicClient createClient(String endpoint) {
        properties.requireChannelConfig();
        Credential credential = new Credential(properties.getSecretId(), properties.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setConnTimeout(properties.getConnTimeoutSeconds());
        httpProfile.setReqMethod("POST");
        httpProfile.setEndpoint(endpoint);

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setSignMethod("TC3-HMAC-SHA256");
        clientProfile.setHttpProfile(httpProfile);
        return new EssbasicClient(credential, properties.getRegion(), clientProfile);
    }
}
