package com.mtfm.deadman.system.dto.user;

import com.mtfm.deadman.common.validation.PhoneNumber;
import com.mtfm.deadman.common.validation.UserStatusValue;
import com.mtfm.deadman.system.dto.org.UserPositionBindingRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 更新用户请求（字段为 null 表示不修改）。
 *
 * @param nickname             昵称
 * @param avatarFileId         头像文件 ID；传 null 表示不修改
 * @param status               用户状态：0-禁用，1-正常（自助接口忽略）
 * @param phone                手机号（自助接口可修改）
 * @param departmentIds        所属部门 ID 列表（仅管理端生效，非 null 时覆盖式更新）
 * @param primaryDepartmentId  主部门 ID（仅管理端生效）
 * @param positionBindings     职位绑定列表（仅管理端生效，非 null 时覆盖式更新）
 */
public record UpdateUserRequest(
        @Size(max = 64, message = "昵称最长 64 字符") String nickname,
        Long avatarFileId,
        @UserStatusValue Integer status,
        @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone,
        List<Long> departmentIds,
        Long primaryDepartmentId,
        List<@Valid UserPositionBindingRequest> positionBindings) {
}
