package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通添加分账接收方命令。
 *
 * @param appid        公众号/小程序 AppId
 * @param type         接收方类型
 * @param account      接收方账号
 * @param relationType 与分账方的关系类型
 */
public record WechatAddProfitSharingReceiverCommand(
        String appid, String type, String account, String relationType) {
}
