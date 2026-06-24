package com.mtfm.deadman.plugin.logistics.spi.common;

/**
 * 物流收寄件联系人信息。
 *
 * @param name        姓名
 * @param mobile      手机号
 * @param printAddress 完整打印地址
 */
public record LogisticsContactInfo(String name, String mobile, String printAddress) {
}
