package com.mtfm.deadman.system.vo.org;

import java.time.LocalDateTime;

/**
 * 部门详情。
 * 
 * @param id         部门主键
 * @param parentId   上级部门 ID
 * @param deptCode   部门编码
 * @param deptName   部门名称
 * @param sortOrder  排序号
 * @param status     状态
 * @param createTime 创建时间
 * @param updateTime 更新时间
 */
public record DepartmentVO(
                Long id,
                Long parentId,
                String deptCode,
                String deptName,
                Integer sortOrder,
                Integer status,
                LocalDateTime createTime,
                LocalDateTime updateTime) {
}
