package com.mtfm.deadman.plugin.wechat.web.service;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.web.WechatWebConstants;
import com.mtfm.deadman.plugin.wechat.web.config.WechatWebPluginProperties;
import com.mtfm.deadman.plugin.wechat.web.vo.WechatWebAuthorizeUrlVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 微信网页扫码登录授权地址生成服务。
 */
@Service
@RequiredArgsConstructor
public class WechatWebAuthorizeService {

    private final WechatWebPluginProperties properties;
    private final WechatWebLoginStateStore stateStore;

    /**
     * 生成扫码登录授权页地址与 state。
     *
     * @return 授权地址与 state
     */
    public WechatWebAuthorizeUrlVO createAuthorizeUrl() {
        requireConfigured();
        String state = stateStore.issueState();
        String authorizeUrl = UriComponentsBuilder.fromUriString(WechatWebConstants.QR_CONNECT_BASE_URL)
                .queryParam("appid", properties.getAppId())
                .queryParam("redirect_uri", properties.getRedirectUri().trim())
                .queryParam("response_type", "code")
                .queryParam("scope", "snsapi_login")
                .queryParam("state", state)
                .build()
                .toUriString() + "#wechat_redirect";
        return new WechatWebAuthorizeUrlVO(authorizeUrl, state, stateStore.stateExpiresInSeconds());
    }

    /**
     * 检查网站应用必要配置是否齐全。
     */
    private void requireConfigured() {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "未配置微信网页应用 AppId/AppSecret");
        }
        if (!StringUtils.hasText(properties.getRedirectUri())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "未配置微信网页应用 redirect-uri");
        }
    }
}
