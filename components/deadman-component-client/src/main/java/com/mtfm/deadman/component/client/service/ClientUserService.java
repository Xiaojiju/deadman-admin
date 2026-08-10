package com.mtfm.deadman.component.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.dto.UpdateClientUserProfileRequest;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.mapper.ClientUserBaseMapper;
import com.mtfm.deadman.component.client.vo.ClientUserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户端用户资料查询与自助更新。
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
        ClientUserBase userBase = requireByUserCode(userCode);
        return toProfileVO(userBase);
    }

    /**
     * 当前用户更新本人资料（仅 nickname、avatar；未传字段不修改）。
     *
     * @param userId 当前用户 ID
     * @param request 更新请求
     * @return 更新后的用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    public ClientUserProfileVO updateMyProfile(Long userId, UpdateClientUserProfileRequest request) {
        ClientUserBase user = requireById(userId);
        boolean changed = false;
        if (request.nickname() != null) {
            String nickname = request.nickname().trim();
            if (!StringUtils.hasText(nickname)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "昵称不能为空");
            }
            user.setNickname(nickname);
            changed = true;
        }
        if (request.avatar() != null) {
            String avatar = request.avatar().trim();
            user.setAvatar(StringUtils.hasText(avatar) ? avatar : null);
            changed = true;
        }
        if (changed) {
            updateById(user);
        }
        return toProfileVO(user);
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
     * @param userBase 用户基础信息
     * @param loginIdentifier 登录标识
     * @return 登录用户
     */
    public ClientLoginUser buildLoginUser(ClientUserBase userBase, String loginIdentifier) {
        boolean enabled = userBase.getStatus() != null && userBase.getStatus() == UserStatus.ACTIVE.getValue();
        return new ClientLoginUser(userBase.getId(), userBase.getUserCode(), loginIdentifier, userBase.getNickname(),
            enabled, List.of());
    }

    private ClientUserBase requireByUserCode(String userCode) {
        ClientUserBase userBase = lambdaQuery().eq(ClientUserBase::getUserCode, userCode).one();
        if (userBase == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return userBase;
    }

    private ClientUserProfileVO toProfileVO(ClientUserBase userBase) {
        String username = resolveUsername(userBase.getId());
        return new ClientUserProfileVO(userBase.getUserCode(), username, userBase.getNickname(), userBase.getAvatar(),
            userBase.getStatus());
    }

    private String resolveUsername(Long userId) {
        ClientUserAccount account = clientUserAccountService
            .getOne(new LambdaQueryWrapper<ClientUserAccount>().eq(ClientUserAccount::getUserId, userId)
                .eq(ClientUserAccount::getAccountType, AccountType.USERNAME.getCode()));
        return account == null ? null : account.getAccountIdentifier();
    }
}
