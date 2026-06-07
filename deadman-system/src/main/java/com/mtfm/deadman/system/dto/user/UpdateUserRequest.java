package com.mtfm.deadman.system.dto.user;

import com.mtfm.deadman.common.validation.PhoneNumber;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 更新用户请求（字段为 null 表示不修改）。
 *
 * @param nickname     昵称
 * @param avatar       头像 URL
 * @param status       用户状态：0-禁用，1-正常（自助接口忽略）
 * @param phone        手机号（自助接口可修改）
 * @param departmentId 所属部门 ID（仅管理端生效）
 * @param positionIds  职位 ID 列表（仅管理端生效，非 null 时覆盖式更新）
 */
public record UpdateUserRequest(
        @Size(max = 64, message = "昵称最长 64 字符") String nickname,
        @Size(max = 512, message = "头像 URL 最长 512 字符") String avatar,
        Integer status,
        @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone,
        Long departmentId,
        List<Long> positionIds) {
}
