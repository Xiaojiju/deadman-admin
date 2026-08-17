package com.mtfm.deadman.plugin.wechat.miniprogram.spi;

import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBoundPhoneVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatResolvedPhoneVO;

/**
 * 微信手机号 SPI：换号、绑定与查询，由各用户体系桥接模块实现。
 */
public interface WechatPhoneBindingHandler {

    /**
     * 所属登录 Provider 组标识。
     *
     * @return 组标识
     */
    String loginGroupId();

    /**
     * 使用 getPhoneNumber code 向微信换取手机号（不落库，供注册页回填）。
     *
     * @param request 含手机号动态令牌 code
     * @return 明文手机号
     */
    WechatResolvedPhoneVO resolvePhone(WechatBindPhoneRequest request);

    /**
     * 绑定微信手机号到指定用户。
     *
     * @param userId  用户主键
     * @param request 绑定请求（含 getPhoneNumber code）
     * @return 绑定结果
     */
    WechatBindPhoneVO bindPhone(Long userId, WechatBindPhoneRequest request);

    /**
     * 查询指定用户当前已绑定的手机号（脱敏）。
     *
     * @param userId 用户主键
     * @return 已绑定手机号；未绑定时 {@code phone} 为 null
     */
    WechatBoundPhoneVO getBoundPhone(Long userId);
}
