package com.mtfm.deadman.plugin.wechat.login.resolver;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatMiniprogramLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;
import com.mtfm.deadman.plugin.wechat.login.session.WechatMiniprogramLoginSession;
import com.mtfm.deadman.plugin.wechat.login.spi.WechatLoginResolver;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatCode2SessionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 微信小程序登录解析器。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-miniprogram", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MiniprogramWechatLoginResolver implements WechatLoginResolver {

    private final WechatApiClient wechatApiClient;

    @Override
    public String loginKind() {
        return WechatLoginKinds.MINIPROGRAM;
    }

    @Override
    public boolean supports(WechatLoginCredential credential) {
        return credential instanceof WechatMiniprogramLoginCredential;
    }

    @Override
    public WechatLoginSession resolve(WechatLoginCredential credential) {
        if (!(credential instanceof WechatMiniprogramLoginCredential miniprogramCredential)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的微信小程序登录凭证");
        }
        if (!StringUtils.hasText(miniprogramCredential.code())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 code 不能为空");
        }
        WechatCode2SessionResult session = wechatApiClient.code2Session(miniprogramCredential.code().trim());
        return new WechatMiniprogramLoginSession(session.openid(), session.unionid(), session.sessionKey());
    }
}
