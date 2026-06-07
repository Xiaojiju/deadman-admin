package com.mtfm.deadman.system.vo.org;

import java.util.List;

/**
 * 部门树节点。
 *
 * @param id       部门主键
 * @param parentId 上级部门 ID
 * @param deptCode 部门编码
 * @param deptName 部门名称
 * @param sortOrder 排序号
 * @param status   状态：0-禁用，1-启用
 * @param children 下级部门列表
 */
public record DepartmentTreeVO(
        Long id,
        Long parentId,
        String deptCode,
        String deptName,
        Integer sortOrder,
        Integer status,
        List<DepartmentTreeVO> children) {
}
