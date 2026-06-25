package com.mtfm.deadman.plugin.im.tencent.client;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock 腾讯云 IM 网关，用于本地与测试环境无 SecretKey 时验收 IM 链路。
 */
@Slf4j
public class MockTencentImApiGateway implements TencentImApiGateway {

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateUserSig(String imUserId) {
        return "mock-usersig-" + imUserId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importAccount(String imUserId, String nickname, String avatarUrl) {
        log.info(
                "Mock 导入 IM 账号：imUserId={}, nickname={}, avatarUrl={}",
                imUserId,
                nickname,
                StringUtils.hasText(avatarUrl) ? avatarUrl : "-");
    }
}
