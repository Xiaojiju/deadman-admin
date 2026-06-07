package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.constants.CacheNames;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.dto.user.UpdateUserRequest;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.mapper.UserBaseMapper;
import com.mtfm.deadman.system.vo.user.UserAccountBindingVO;
import com.mtfm.deadman.system.vo.user.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户资料查询与状态校验。
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserBaseMapper, UserBase> {

    private final UserAccountService userAccountService;
    private final UserOrgService userOrgService;
    private final UserPositionService userPositionService;

    /**
     * 按用户编码查询资料（带缓存）。
     *
     * @param userCode 用户编码
     * @return 用户资料
     */
    @Cacheable(value = CacheNames.USER_PROFILE, key = "#userCode")
    public UserProfileVO getProfileByUserCode(String userCode) {
        UserBase userBase = lambdaQuery().eq(UserBase::getUserCode, userCode).one();
        if (userBase == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toProfileVO(userBase);
    }

    /**
     * 失效用户资料缓存。
     *
     * @param userCode 用户编码
     */
    @CacheEvict(value = CacheNames.USER_PROFILE, key = "#userCode")
    public void evictProfileCache(String userCode) {
    }

    /**
     * 当前用户更新本人资料（昵称、头像、手机号；忽略 status、部门、职位）。
     *
     * @param userCode 用户编码
     * @param request  更新请求
     * @return 更新后的用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheNames.USER_PROFILE, key = "#userCode")
    public UserProfileVO updateProfileByUserCode(String userCode, UpdateUserRequest request) {
        UserBase user = lambdaQuery().eq(UserBase::getUserCode, userCode).one();
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        boolean changed = false;
        if (request.nickname() != null) {
            user.setNickname(request.nickname());
            changed = true;
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
            changed = true;
        }
        if (request.phone() != null) {
            userAccountService.bindOrUpdatePhone(user.getId(), request.phone());
            changed = true;
        }
        if (changed) {
            updateById(user);
        }
        return toProfileVO(user);
    }

    /**
     * 校验用户存在且处于正常状态。
     *
     * @param userBase 用户实体
     */
    public void assertUserActive(UserBase userBase) {
        if (userBase == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
    }

    private UserProfileVO toProfileVO(UserBase userBase) {
        List<UserAccount> accountEntities = userAccountService.list(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUserId, userBase.getId())
                .orderByAsc(UserAccount::getAccountType)
                .orderByAsc(UserAccount::getCreateTime));

        List<UserAccountBindingVO> accounts = accountEntities.stream().map(this::toAccountBinding).toList();
        String username = accountEntities.stream()
                .filter(a -> AccountType.USERNAME.getCode().equals(a.getAccountType()))
                .map(UserAccount::getAccountIdentifier)
                .findFirst()
                .orElse(null);
        String phone = userAccountService.findPhoneByUserId(userBase.getId());

        return UserProfileVO.builder()
                .userCode(userBase.getUserCode())
                .username(username)
                .nickname(userBase.getNickname())
                .avatar(userBase.getAvatar())
                .phone(phone)
                .department(userOrgService.toDepartmentRef(userBase.getDepartmentId()))
                .positions(userPositionService.getPositionRefsByUserId(userBase.getId()))
                .status(userBase.getStatus())
                .accounts(accounts)
                .createTime(userBase.getCreateTime())
                .build();
    }

    private UserAccountBindingVO toAccountBinding(UserAccount account) {
        return new UserAccountBindingVO(
                account.getAccountType(),
                account.getAccountIdentifier(),
                account.getOauthProvider(),
                account.getVerified(),
                account.getStatus());
    }
}
