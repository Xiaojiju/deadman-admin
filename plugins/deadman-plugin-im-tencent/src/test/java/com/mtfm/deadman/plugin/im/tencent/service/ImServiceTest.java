package com.mtfm.deadman.plugin.im.tencent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mtfm.deadman.plugin.im.tencent.client.MockTencentImApiGateway;
import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;
import com.mtfm.deadman.plugin.im.tencent.entity.ImUserAccount;
import com.mtfm.deadman.plugin.im.tencent.manager.ImUserRealmBridgeRegistry;
import com.mtfm.deadman.plugin.im.tencent.mapper.ImUserAccountMapper;
import com.mtfm.deadman.plugin.im.tencent.spi.ImSubject;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserProfileSource;
import com.mtfm.deadman.plugin.im.tencent.spi.ImUserRealmBridge;
import com.mtfm.deadman.plugin.im.tencent.util.ImUserIdFormatter;
import com.mtfm.deadman.plugin.im.tencent.vo.ImCredentialVO;

/**
 * {@link ImService} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ImServiceTest {

    @Mock
    private ImUserAccountMapper imUserAccountMapper;

    @Mock
    private ImUserRealmBridge bridge;

    private ImService imService;

    @BeforeEach
    void setUp() {
        ImTencentPluginProperties properties = new ImTencentPluginProperties();
        properties.setMockEnabled(true);
        properties.setSdkAppId(1400L);
        properties.setUserSigExpireSeconds(3600L);
        when(bridge.realmId()).thenReturn("client");
        ImUserRealmBridgeRegistry registry = new ImUserRealmBridgeRegistry(List.of(bridge));
        imService = new ImService(
                properties,
                registry,
                imUserAccountMapper,
                new ImUserIdFormatter(properties),
                new MockTencentImApiGateway());
    }

    @Test
    void shouldIssueCredentialAndImportAccount() {
        ImSubject subject = new ImSubject("client", "CL20260001");
        when(bridge.resolveProfileSource(subject))
                .thenReturn(new ImUserProfileSource("测试用户", "https://cdn.example.com/a.png", true));
        when(imUserAccountMapper.selectOne(any())).thenReturn(null);
        when(imUserAccountMapper.insert(any(ImUserAccount.class))).thenAnswer(invocation -> {
            ImUserAccount account = invocation.getArgument(0);
            account.setId(1L);
            return 1;
        });

        ImCredentialVO credential = imService.issueCredential(subject);

        assertThat(credential.imUserId()).isEqualTo("client_CL20260001");
        assertThat(credential.userSig()).isEqualTo("mock-usersig-client_CL20260001");
        assertThat(credential.sdkAppId()).isEqualTo(1400L);

        ArgumentCaptor<ImUserAccount> insertCaptor = ArgumentCaptor.forClass(ImUserAccount.class);
        verify(imUserAccountMapper).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().getNickname()).isEqualTo("测试用户");
    }

    @Test
    void shouldIssueCredentialForCurrentUser() {
        ImSubject subject = new ImSubject("client", "CL20260001");
        when(bridge.resolveCurrentSubject()).thenReturn(Optional.of(subject));
        when(bridge.resolveProfileSource(subject))
                .thenReturn(new ImUserProfileSource("测试用户", null, true));
        when(imUserAccountMapper.selectOne(any())).thenReturn(ImUserAccount.builder()
                .id(1L)
                .realmId("client")
                .subjectId("CL20260001")
                .imUserId("client_CL20260001")
                .nickname("测试用户")
                .build());

        ImCredentialVO credential = imService.issueCredentialForCurrentUser("client");

        assertThat(credential.imUserId()).isEqualTo("client_CL20260001");
    }
}
