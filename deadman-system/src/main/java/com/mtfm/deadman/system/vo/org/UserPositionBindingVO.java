package com.mtfm.deadman.system.vo.org;

/**
 * 用户在部门下的职位绑定展示。
 *
 * @param department 部门引用
 * @param position   职位引用
 */
public record UserPositionBindingVO(OrgRefVO department, OrgRefVO position) {
}
