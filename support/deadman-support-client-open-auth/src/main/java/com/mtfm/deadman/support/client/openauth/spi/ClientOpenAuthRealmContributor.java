package com.mtfm.deadman.support.client.openauth.spi;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.openauth.spi.OpenAuthRealmContributor;
import org.springframework.stereotype.Component;

/**
 * 用户端开放授权 realm 贡献者。
 */
@Component
public class ClientOpenAuthRealmContributor implements OpenAuthRealmContributor {

    /**
     * 用户端域标识。
     *
     * @return client
     */
    @Override
    public String realmId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    /**
     * 用户端 API 前缀。
     *
     * @return /client/api
     */
    @Override
    public String apiPrefix() {
        return "/client/api";
    }
}
