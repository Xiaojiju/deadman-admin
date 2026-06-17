package com.mtfm.deadman.plugin.wechat.login.resolver;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatWebLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;
import com.mtfm.deadman.plugin.wechat.login.session.WechatWebLoginSession;
import com.mtfm.deadman.plugin.wechat.login.spi.WechatLoginResolver;
import com.mtfm.deadman.plugin.wechat.web.client.WechatOAuth2TokenResult;
import com.mtfm.deadman.plugin.wechat.web.client.WechatWebApiClient;
import com.mtfm.deadman.plugin.wechat.web.client.WechatWebUserInfo;
import com.mtfm.deadman.plugin.wechat.web.service.WechatWebLoginStateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 微信网页扫码登录解析器。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-web", name = "enabled", havingValue = "true")
public class WebWechatLoginResolver implements WechatLoginResolver {

    private final WechatWebApiClient wechatWebApiClient;
    private final WechatWebLoginStateStore stateStore;

    @Override
    public String loginKind() {
        return WechatLoginKinds.WEB;
    }

    @Override
    public boolean supports(WechatLoginCredential credential) {
        return credential instanceof WechatWebLoginCredential;
    }

    @Override
    public WechatLoginSession resolve(WechatLoginCredential credential) {
        if (!(credential instanceof WechatWebLoginCredential webCredential)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的微信网页登录凭证");
        }
        if (!StringUtils.hasText(webCredential.code())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 code 不能为空");
        }
        stateStore.consumeState(webCredential.state());
        WechatOAuth2TokenResult tokenResult = wechatWebApiClient.oauth2AccessToken(webCredential.code().trim());
        WechatWebUserInfo userInfo = wechatWebApiClient.getUserInfo(tokenResult.accessToken(), tokenResult.openid());
        return new WechatWebLoginSession(
                userInfo.openid(),
                userInfo.unionid(),
                userInfo.nickname(),
                userInfo.headimgurl(),
                tokenResult.accessToken());
    }
}
