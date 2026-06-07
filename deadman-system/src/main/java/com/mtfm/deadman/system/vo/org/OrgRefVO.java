package com.mtfm.deadman.system.vo.org;

/**
 * 部门或职位简要引用。
 *
 * @param id   主键
 * @param code 编码
 * @param name 名称
 */
public record OrgRefVO(Long id, String code, String name) {
}
