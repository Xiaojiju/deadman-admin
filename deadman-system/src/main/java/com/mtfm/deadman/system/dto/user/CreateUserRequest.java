package com.mtfm.deadman.system.dto.user;

import com.mtfm.deadman.common.validation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 管理端新增用户请求。
 *
 * @param username     用户名
 * @param password     初始密码
 * @param nickname     昵称
 * @param avatar       头像 URL
 * @param phone        手机号
 * @param departmentId 所属部门 ID
 * @param positionIds  职位 ID 列表，可绑定多个
 */
public record CreateUserRequest(
        @NotBlank(message = "用户名不能为空") @Size(min = 3, max = 64, message = "用户名长度为 3-64") String username,
        @NotBlank(message = "密码不能为空") @Size(min = 8, max = 128, message = "密码长度为 8-128") String password,
        @Size(max = 64, message = "昵称最长 64 字符") String nickname,
        @Size(max = 512, message = "头像 URL 最长 512 字符") String avatar,
        @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone,
        Long departmentId,
        List<Long> positionIds) {
}
