package com.mtfm.deadman.system.dto.user;

import com.mtfm.deadman.common.validation.PhoneNumber;
import com.mtfm.deadman.system.dto.org.UserPositionBindingRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 管理端新增用户请求。
 *
 * @param username             用户名
 * @param password             初始密码
 * @param nickname             昵称
 * @param avatar               头像 URL
 * @param phone                手机号
 * @param departmentIds        所属部门 ID 列表
 * @param primaryDepartmentId  主部门 ID，为空时取 departmentIds 首项
 * @param positionBindings     用户在各部门下的职位绑定
 */
public record CreateUserRequest(
        @NotBlank(message = "用户名不能为空") @Size(min = 3, max = 64, message = "用户名长度为 3-64") String username,
        @NotBlank(message = "密码不能为空") @Size(min = 8, max = 128, message = "密码长度为 8-128") String password,
        @Size(max = 64, message = "昵称最长 64 字符") String nickname,
        @Size(max = 512, message = "头像 URL 最长 512 字符") String avatar,
        @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone,
        List<Long> departmentIds,
        Long primaryDepartmentId,
        List<@Valid UserPositionBindingRequest> positionBindings) {
}
