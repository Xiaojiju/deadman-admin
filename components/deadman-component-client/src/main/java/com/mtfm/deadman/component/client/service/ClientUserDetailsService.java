package com.mtfm.deadman.component.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户端 JWT 认证时按 userCode 加载用户。
 */
@Service
@RequiredArgsConstructor
public class ClientUserDetailsService implements UserDetailsService {

    private final ClientUserService clientUserService;
    private final ClientUserAccountService clientUserAccountService;

    /**
     * 按用户编码加载用户详情。
     *
     * @param userCode 用户编码
     * @return 用户详情
     */
    @Override
    public UserDetails loadUserByUsername(String userCode) throws UsernameNotFoundException {
        ClientUserBase userBase = clientUserService.lambdaQuery()
                .eq(ClientUserBase::getUserCode, userCode)
                .one();
        if (userBase == null) {
            throw new UsernameNotFoundException("用户不存在: " + userCode);
        }
        ClientUserAccount account = clientUserAccountService.getOne(new LambdaQueryWrapper<ClientUserAccount>()
                .eq(ClientUserAccount::getUserId, userBase.getId())
                .eq(ClientUserAccount::getAccountType, AccountType.USERNAME.getCode()));
        String username = account != null ? account.getAccountIdentifier() : null;
        ClientLoginUser loginUser = clientUserService.buildLoginUser(userBase, username);
        return loginUser;
    }
}
