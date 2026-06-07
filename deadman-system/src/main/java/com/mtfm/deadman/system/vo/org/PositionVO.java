package com.mtfm.deadman.system.vo.org;

import java.time.LocalDateTime;

/**
 * 职位详情。
 *
 * @param id           职位主键
 * @param departmentId 所属部门 ID，null 表示全局职位
 * @param positionCode 职位编码
 * @param positionName 职位名称
 * @param sortOrder    排序号
 * @param status       状态：0-禁用，1-启用
 * @param createTime   创建时间
 * @param updateTime   更新时间
 */
public record PositionVO(
        Long id,
        Long departmentId,
        String positionCode,
        String positionName,
        Integer sortOrder,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
