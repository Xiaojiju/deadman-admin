package com.mtfm.deadman.component.client.wechat;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatPhoneInfo;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.spi.WechatPhoneBindingHandler;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户端微信手机号绑定实现，将微信插件与用户端账号体系桥接。
 */
@Component
@RequiredArgsConstructor
public class ClientWechatPhoneBindingHandler implements WechatPhoneBindingHandler {

    private final WechatApiClient wechatApiClient;
    private final ClientUserAccountService clientUserAccountService;

    /**
     * 使用 getPhoneNumber code 获取手机号并绑定到用户端账号。
     *
     * @param userId  用户主键
     * @param request 绑定请求
     * @return 绑定结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatBindPhoneVO bindPhone(Long userId, WechatBindPhoneRequest request) {
        WechatPhoneInfo phoneInfo = wechatApiClient.getPhoneNumber(request.code());
        String phone = StringUtils.hasText(phoneInfo.purePhoneNumber())
                ? phoneInfo.purePhoneNumber()
                : phoneInfo.phoneNumber();
        clientUserAccountService.bindOrUpdatePhone(userId, phone);
        return new WechatBindPhoneVO(phone);
    }

    @Override
    public String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }
}
