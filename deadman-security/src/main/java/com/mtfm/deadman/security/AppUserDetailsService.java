package com.mtfm.deadman.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.mapper.UserBaseMapper;
import com.mtfm.deadman.security.service.AuthPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 根据 userCode 加载当前登录用户（含角色与权限码），供 JWT 过滤器与 Security 使用
 * <p>
 * 用户绑定多角色时，角色编码与权限码均会去重合并。
 */
@Service
@Primary
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserBaseMapper userBaseMapper;
    private final AuthPermissionService authPermissionService;

    /**
     * 根据 userCode 加载当前登录用户（含角色与权限码）
     * 
     * @param username 此处为 userCode（对外用户编码），非数据库主键
     * @return 当前登录用户
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserBase userBase = userBaseMapper.selectOne(
                new LambdaQueryWrapper<UserBase>().eq(UserBase::getUserCode, username));
        if (userBase == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return authPermissionService.buildLoginUser(userBase);
    }
}
