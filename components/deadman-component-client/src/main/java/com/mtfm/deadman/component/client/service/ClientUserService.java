package com.mtfm.deadman.component.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.mapper.ClientUserBaseMapper;
import com.mtfm.deadman.component.client.vo.ClientUserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户端用户资料查询。
 */
@Service
@RequiredArgsConstructor
public class ClientUserService extends ServiceImpl<ClientUserBaseMapper, ClientUserBase> {

    private final ClientUserAccountService clientUserAccountService;

    /**
     * 按用户编码查询资料。
     *
     * @param userCode 用户编码
     * @return 用户资料
     */
    public ClientUserProfileVO getProfileByUserCode(String userCode) {
        ClientUserBase userBase = lambdaQuery().eq(ClientUserBase::getUserCode, userCode).one();
        if (userBase == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String username = resolveUsername(userBase.getId());
        return new ClientUserProfileVO(
                userBase.getUserCode(), username, userBase.getNickname(), userBase.getAvatar(), userBase.getStatus());
    }

    /**
     * 按 ID 获取用户，不存在时抛业务异常。
     *
     * @param userId 用户 ID
     * @return 用户基础信息
     */
    public ClientUserBase requireById(Long userId) {
        ClientUserBase user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 构建登录用户负载体。
     *
     * @param userBase        用户基础信息
     * @param loginIdentifier 登录标识
     * @return 登录用户
     */
    public ClientLoginUser buildLoginUser(ClientUserBase userBase, String loginIdentifier) {
        boolean enabled = userBase.getStatus() != null && userBase.getStatus() == UserStatus.ACTIVE.getValue();
        return new ClientLoginUser(
                userBase.getId(),
                userBase.getUserCode(),
                loginIdentifier,
                userBase.getNickname(),
                enabled,
                List.of());
    }

    private String resolveUsername(Long userId) {
        ClientUserAccount account = clientUserAccountService.getOne(new LambdaQueryWrapper<ClientUserAccount>()
                .eq(ClientUserAccount::getUserId, userId)
                .eq(ClientUserAccount::getAccountType, AccountType.USERNAME.getCode()));
        return account == null ? null : account.getAccountIdentifier();
    }
}
