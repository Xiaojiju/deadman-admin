package com.mtfm.deadman.plugin.wechat.miniprogram.service;

import com.mtfm.deadman.component.client.ClientAuthSupport;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatPhoneInfo;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 微信小程序手机号获取与绑定。
 */
@Service
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-miniprogram", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class WechatMiniprogramPhoneService {

    private final WechatApiClient wechatApiClient;
    private final ClientUserAccountService clientUserAccountService;

    /**
     * 使用 getPhoneNumber 返回的 code 获取手机号并绑定到当前用户。
     *
     * @param loginUser 当前登录用户
     * @param request   绑定请求
     * @return 绑定结果
     */
    @Transactional(rollbackFor = Exception.class)
    public WechatBindPhoneVO bindPhone(ClientLoginUser loginUser, WechatBindPhoneRequest request) {
        ClientLoginUser user = ClientAuthSupport.requireLogin(loginUser);
        WechatPhoneInfo phoneInfo = wechatApiClient.getPhoneNumber(request.code());
        String phone = StringUtils.hasText(phoneInfo.purePhoneNumber())
                ? phoneInfo.purePhoneNumber()
                : phoneInfo.phoneNumber();
        clientUserAccountService.bindOrUpdatePhone(user.getUserId(), phone);
        return new WechatBindPhoneVO(phone);
    }
}
