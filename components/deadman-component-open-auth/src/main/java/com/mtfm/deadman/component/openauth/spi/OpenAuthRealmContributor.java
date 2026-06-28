package com.mtfm.deadman.component.openauth.spi;

/**
 * 开放授权用户域贡献者，各用户体系模块注册 realm 与 API 前缀。
 */
public interface OpenAuthRealmContributor {

    /**
     * 用户域标识，如 client、admin。
     *
     * @return 域标识
     */
    String realmId();

    /**
     * 该域 API 前缀，如 /client/api。
     *
     * @return API 前缀
     */
    String apiPrefix();
}
