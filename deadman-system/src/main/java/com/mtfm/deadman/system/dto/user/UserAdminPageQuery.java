package com.mtfm.deadman.system.dto.user;

import com.mtfm.deadman.common.page.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端用户列表查询：通用分页 + 业务筛选。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserAdminPageQuery extends PageParam {
    /**
     * 关键词
     */
    private String keyword;
    /**
     * 状态
     */
    private Integer status;
}
