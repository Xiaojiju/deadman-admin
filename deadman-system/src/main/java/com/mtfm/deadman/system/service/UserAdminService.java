package com.mtfm.deadman.system.service;

import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.system.dto.role.AssignUserRolesRequest;
import com.mtfm.deadman.system.dto.user.CreateUserRequest;
import com.mtfm.deadman.system.dto.user.ResetUserPasswordRequest;
import com.mtfm.deadman.system.dto.user.UpdateUserRequest;
import com.mtfm.deadman.system.dto.user.UserAdminPageQuery;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import com.mtfm.deadman.system.vo.user.UserAdminSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理端用户门面：委托读/写子服务，保持 Controller 依赖单一入口。
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserAdminQueryService userAdminQueryService;
    private final UserAdminMutationService userAdminMutationService;

    /**
     * 分页查询用户列表。
     *
     * @param query 分页与筛选条件
     * @return 用户分页列表
     */
    public PageVO<UserAdminSummaryVO> pageUsers(UserAdminPageQuery query) {
        return userAdminQueryService.pageUsers(query);
    }

    /**
     * 用户详情。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    public UserAdminDetailVO getUserDetail(Long userId) {
        return userAdminQueryService.getUserDetail(userId);
    }

    /**
     * 新增用户并绑定默认 USER 角色。
     *
     * @param request 新增用户请求
     * @return 新建用户详情
     */
    public UserAdminDetailVO createUser(CreateUserRequest request) {
        return userAdminMutationService.createUser(request);
    }

    /**
     * 更新昵称、头像、状态、手机号、部门或职位。
     *
     * @param userId  用户 ID
     * @param request 更新请求
     * @return 更新后的用户详情
     */
    public UserAdminDetailVO updateUser(Long userId, UpdateUserRequest request) {
        return userAdminMutationService.updateUser(userId, request);
    }

    /**
     * 逻辑删除用户。
     *
     * @param userId 用户 ID
     */
    public void deleteUser(Long userId) {
        userAdminMutationService.deleteUser(userId);
    }

    /**
     * 为用户分配角色（覆盖式）。
     *
     * @param userId  用户 ID
     * @param request 角色 ID 列表
     * @return 更新后的用户详情
     */
    public UserAdminDetailVO assignUserRoles(Long userId, AssignUserRolesRequest request) {
        return userAdminMutationService.assignUserRoles(userId, request);
    }

    /**
     * 管理端重置用户密码（用于用户忘记密码等场景）。
     *
     * @param userId  用户 ID
     * @param request 新密码请求
     */
    public void resetUserPassword(Long userId, ResetUserPasswordRequest request) {
        userAdminMutationService.resetUserPassword(userId, request);
    }
}
