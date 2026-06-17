package com.mtfm.deadman.plugin.wechat.login.initiator;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;
import com.mtfm.deadman.plugin.wechat.login.initiate.WechatWebLoginInitiateResult;
import com.mtfm.deadman.plugin.wechat.login.spi.WechatLoginInitiator;
import com.mtfm.deadman.plugin.wechat.web.service.WechatWebAuthorizeService;
import com.mtfm.deadman.plugin.wechat.web.vo.WechatWebAuthorizeUrlVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 微信网页扫码登录发起器。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-web", name = "enabled", havingValue = "true")
public class WebWechatLoginInitiator implements WechatLoginInitiator {

    private final WechatWebAuthorizeService authorizeService;

    @Override
    public String loginKind() {
        return WechatLoginKinds.WEB;
    }

    @Override
    public WechatWebLoginInitiateResult initiate() {
        WechatWebAuthorizeUrlVO authorizeUrl = authorizeService.createAuthorizeUrl();
        return new WechatWebLoginInitiateResult(
                authorizeUrl.authorizeUrl(), authorizeUrl.state(), authorizeUrl.stateExpiresInSeconds());
    }
}
