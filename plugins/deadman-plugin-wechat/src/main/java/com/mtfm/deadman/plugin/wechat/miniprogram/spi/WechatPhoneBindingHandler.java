package com.mtfm.deadman.plugin.wechat.miniprogram.spi;

import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;

/**
 * 微信手机号绑定 SPI，各用户体系模块实现后将微信手机号绑定到本体系账号。
 */
public interface WechatPhoneBindingHandler {

    /**
     * 所属登录 Provider 组标识。
     *
     * @return 组标识
     */
    String loginGroupId();

    /**
     * 绑定微信手机号到指定用户。
     *
     * @param userId  用户主键
     * @param request 绑定请求（含 getPhoneNumber code）
     * @return 绑定结果
     */
    WechatBindPhoneVO bindPhone(Long userId, WechatBindPhoneRequest request);
}
