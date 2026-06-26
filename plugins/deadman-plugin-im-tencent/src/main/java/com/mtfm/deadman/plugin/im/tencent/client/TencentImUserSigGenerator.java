package com.mtfm.deadman.plugin.im.tencent.client;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.im.tencent.client.tls.TencentImTlsSigApiV2;
import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;

import lombok.RequiredArgsConstructor;

/**
 * 腾讯云 IM UserSig 生成器，委托官方 TLSSigAPIv2 算法实现。
 */
@RequiredArgsConstructor
public class TencentImUserSigGenerator {

    private final ImTencentPluginProperties properties;

    /**
     * 生成 UserSig。
     *
     * @param imUserId      IM UserID
     * @param expireSeconds 有效期（秒）
     * @return UserSig
     */
    public String generate(String imUserId, long expireSeconds) {
        if (!StringUtils.hasText(imUserId)) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "IM UserID 不能为空");
        }
        properties.requireProductionConfig();
        TencentImTlsSigApiV2 sigApi = new TencentImTlsSigApiV2(properties.getSdkAppId(), properties.getSecretKey());
        String userSig = sigApi.genUserSig(imUserId, expireSeconds);
        if (!StringUtils.hasText(userSig)) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "UserSig 生成失败");
        }
        return userSig;
    }
}
