package com.mtfm.deadman.system.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mtfm.deadman.system.vo.org.OrgRefVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户资料（不含内部用户 ID）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    /** 用户编码 */
    private String userCode;
    /** 主登录用户名（USERNAME 类型账号标识，未绑定时为 null） */
    private String username;
    /** 用户昵称 */
    private String nickname;
    /** 头像 URL */
    private String avatar;
    /** 绑定手机号 */
    private String phone;
    /** 所属部门 */
    private OrgRefVO department;
    /** 职位列表 */
    private List<OrgRefVO> positions;
    /** 用户状态：0-禁用 1-正常 */
    private Integer status;
    /** 已绑定的登录账号列表（含第三方 OAuth） */
    private List<UserAccountBindingVO> accounts;
    /** 创建时间 */
    private LocalDateTime createTime;
}
