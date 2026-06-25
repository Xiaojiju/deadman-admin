package com.mtfm.deadman.plugin.im.tencent.spi;

import java.util.Optional;

/**
 * IM 用户域桥接 SPI：各用户体系通过 Support/Component 实现并注册为 Spring Bean。
 */
public interface ImUserRealmBridge {

    /**
     * 用户域标识，全局唯一，如 client、admin。
     *
     * @return 用户域标识
     */
    String realmId();

    /**
     * 从当前请求/security 上下文解析登录主体。
     *
     * @return 当前登录主体，无登录上下文时返回 empty
     */
    Optional<ImSubject> resolveCurrentSubject();

    /**
     * 解析 IM 展示资料，供账号同步使用。
     *
     * @param subject 抽象用户主体
     * @return 资料快照
     */
    ImUserProfileSource resolveProfileSource(ImSubject subject);
}
