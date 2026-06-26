package com.mtfm.deadman.plugin.wechat.miniprogram.spi;

import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyInitiateRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyQueryRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyInitiateVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyQueryVO;

/**
 * 微信人脸核身 SPI，各用户体系模块实现后将核身能力桥接到本体系账号。
 */
public interface WechatFaceVerifyHandler {

    /**
     * 所属登录 Provider 组标识。
     *
     * @return 组标识
     */
    String loginGroupId();

    /**
     * 发起人脸核身，获取 verifyId 供小程序调用 wx.requestFacialVerify。
     *
     * @param userId  用户主键
     * @param request 发起请求
     * @return verifyId 及业务流水号
     */
    WechatFaceVerifyInitiateVO initiateVerify(Long userId, WechatFaceVerifyInitiateRequest request);

    /**
     * 查询人脸核身结果。
     *
     * @param userId  用户主键
     * @param request 查询请求
     * @return 核身结果
     */
    WechatFaceVerifyQueryVO queryVerifyResult(Long userId, WechatFaceVerifyQueryRequest request);
}
