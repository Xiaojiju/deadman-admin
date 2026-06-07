package com.mtfm.deadman.component.client.dto;

import com.mtfm.deadman.common.page.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端用户管理分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientUserAdminPageQuery extends PageParam {

    /** 用户状态：0-禁用，1-正常 */
    private Integer status;

    /** 关键词：用户编码、昵称、用户名、手机号 */
    private String keyword;
}
